
# Functional MVVM # 

> This guide is based on the [Advanced MVVM](Advanced-MVVM.md) guide.
> You may want to check it out first if you are not familiar with the MVVM pattern.

One of the most powerful feature built into SwingTree
and its [MVVM property API](https://github.com/globaltcad/sprouts)
is the ability to design the Model-View-ViewModel architecture
with immutable view models and models as a basis for the UI.

Yes, you heard right: **immutable** view models.

This may sound like a contradiction since the view model
defines the state of the UI and the UI changes over time
based on the user's input and application logic.
But it turns out that in the world of functional programming
where large nested immutable data structures are used to model
all kinds of complex systems, the problem of updating 
these data structures has long been solved by
using techniques such as *structural sharing*, *withers*
and *lenses*. 
But let's not get lost in functional buzzword bingo
and look at a simple example to see how this works in practice:

## A simple example ##

Let's say we have a simple calculator UI with two input fields,
an operator selector and a result label.
The full view model code for this example 
is fairly straight forward:

```java
public enum Operator {
    ADD, SUBTRACT, MULTIPLY, DIVIDE
}

public record CalculatorInputs(
    String left, String right, Operator operator
){
    public static CalculatorInputs empty(){return new CalculatorInputs("", "", Operator.ADD);}
    public CalculatorInputs withLeft(String left){return new CalculatorInputs(left, right, operator);}
    public CalculatorInputs withRight(String right){return new CalculatorInputs(left, right, operator);}
    public CalculatorInputs withOperator(Operator operator){return new CalculatorInputs(left, right, operator);}
}

public record CalculatorOutput(
    double result, boolean valid, String error
){
    public static CalculatorOutput empty(){return new CalculatorOutput(0, false, "");}
    public CalculatorOutput withResult(double result){return new CalculatorOutput(result, valid, error);}
    public CalculatorOutput withValid(boolean valid){return new CalculatorOutput(result, valid, error);}
    public CalculatorOutput withError(String error){return new CalculatorOutput(result, valid, error);}
}

public record CalculatorViewModel(
    CalculatorInputs inputs, CalculatorOutput output
){
    public static CalculatorViewModel empty(){return new CalculatorViewModel(CalculatorInputs.empty(), CalculatorOutput.empty());}
    public CalculatorViewModel withInputs(CalculatorInputs inputs){return new CalculatorViewModel(inputs, output);}
    public CalculatorViewModel withOutput(CalculatorOutput output){return new CalculatorViewModel(inputs, output);}
    public CalculatorViewModel runCalculation(){
        try{
            double left = Double.parseDouble(inputs.left());
            double right = Double.parseDouble(inputs.right());
            double result = switch(inputs.operator()){
                case ADD      -> left + right; case SUBTRACT -> left - right;
                case MULTIPLY -> left * right; case DIVIDE   -> left / right;
            };
            return withOutput(output.withResult(result).withValid(true));
        } catch ( NumberFormatException e ) {
            return withOutput(output.withError("Invalid number format").withValid(false));
        } catch ( ArithmeticException e ) {
            return withOutput(output.withError("Division by zero").withValid(false));
        }
    }
}
```

This code defines three immutable record classes
that represent the state of the whole calculator UI.
The `CalculatorInputs` record holds the input values
of the two input fields and the selected operator.
The `CalculatorOutput` record holds the result of the calculation,
a boolean flag indicating if the calculation was successful
and an error message if the calculation failed.

What distinguishes these records from regular Java beans
is that they are immutable and have no setters,
instead they have "withers", which are the equivalent
to "setters" in the world of immutable data structures
and functional programming.
Even the `runCalculation` method returns a new instance
of the `CalculatorViewModel` with the updated output
record, instead of modifying the existing instance.

## The View ##

This is all nice and good, but how do we connect this to the UI?
The key is the `Var` class from the Sprouts library
and the previously mentioned *"withers"* of the view model records.
Check out the following code snippet:

```java
import static swingtree.UI.*;

public static class CalculatorView extends JPanel {
    public CalculatorView(Var<CalculatorViewModel> vm) {
        Var<CalculatorInputs> inputs = vm.zoomTo(CalculatorViewModel::inputs, CalculatorViewModel::withInputs);
        Var<CalculatorOutput> output = vm.zoomTo(CalculatorViewModel::output, CalculatorViewModel::withOutput);
        UI.of(this).withLayout("fill")
        .add("span, center, wrap", html("<h2>Calculator</h2>"))
        .add("pushx, growx, width 60px::",
            textField(inputs.zoomTo(CalculatorInputs::left, CalculatorInputs::withLeft))
        )
        .add("width 40px::",
            comboBox(inputs.zoomTo(CalculatorInputs::operator, CalculatorInputs::withOperator), o ->
                switch ( o ) {
                    case ADD      -> " + "; case SUBTRACT -> " - ";
                    case MULTIPLY -> " * "; case DIVIDE   -> " / ";
                }
            )
        )
        .add("pushx, growx, width 60px::",
            textField(inputs.zoomTo(CalculatorInputs::right, CalculatorInputs::withRight))
        )
        .add("wrap",
            button("Run!").onClick( e -> vm.set(vm.get().runCalculation()) )
        )
        .add("span, center, wrap, height 60px::",
            label(output.viewAsString( result -> {
                if ( result.valid() ) {
                    return "Result: " + result.result();
                } else if ( !result.error().isEmpty() ) {
                    return "Error: " + result.error();
                }
                return "";
            }))
        );
    }
    // For running the example
    public static void main(String... args) {
        Var<CalculatorViewModel> vm = Var.of(CalculatorViewModel.empty());
        UI.show(f->new CalculatorView(vm));
    }
}
```

Running this code will render the following UI:

![Calculator](../img/tutorial/Functional-MVVM-Calculator.png)

Take a moment to read through the code and get a feel for it.
What you may find particularly interesting is the `zoomTo` method.
It returns a so-called *property lens* that allows you to
focus on a specific field on the view model and update it
through one of its "withers". 
You can define these lenses recursively to focus on nested components
of the view model, which is a powerful way to create sub-views for sub-models.

What happens under the hood is that the lens creates a new `Var` property
that is bi-directionally bound to the original `Var` and only
updates the focused field of the view model when the lens is modified through `set`.
You can also register change listeners on the lens to react to changes
to the focused field. Note that lenses are smart, they will only
trigger change events when the focused field actually changes.
So even if the whole view model is updated, only a change of the focused field
will trigger a change event in the lens.

## Where is the Model? ##

You may have noticed that the example does not include a model class.
The reason is simply that this example is too basic to require one.
But in a real-world application you would want to maintain a model
that holds the business logic and data of the internal application like for
example a model for application settings, database entities or
network data. The model would typically be updated by the view model, 
whereas the view does not interact with the model directly.

## Dealing with Lists ##

The above example is nice and all, but what if your application
requires you to model and display multiple things at once,
like for example populating a table with data from a database
or displaying a list of items in a list view?

When implementing classical MVVM in SwingTree,
you can use the observable `Vars` type to represent
a list of items that can be updated and observed.
But when you want to go functional, you don't need
observable lists, you can simple have a property lens
which focuses on a list field on the view model
and updates it through a "wither".
In your view you can then use this lens to create
sub-views for each item in the list and
register change listeners on the lens to react
to changes in the list.

**Simple right? But there is a catch:**

You can't react to specific changes in the list
like you can with observable lists.
So when the list change listener in classical MVVM
reports a removal between the item at index `x` and 
`y`, SwingTree can update the view efficiently by 
removing the sub-views for the items at index `x` to `y`.
But when doing this with a lens on a list field,
you would have to rebuild all sub-views for the list
from scratch, which is not very efficient.

**Does that matter?**

It depends on the size of the list
and the complexity of the sub-views.
But in general, the performance of the lens approach
is good enough for the vast majority of use cases,
and the gained benefits of immutability and
functional programming are well worth it.
And also don't forget that besides better maintainability
and testability, these benefits also encompass
opportunities for performance optimizations
through parallelism, lazy evaluation, structural sharing
and caching.






