package swingtree.style;

import swingtree.UI;

import javax.swing.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class StyleSheet
{
    private final Supplier<Style> _defaultStyle;
    private final Map<StyleTrait, List<StyleTrait>> _traitGraph = new LinkedHashMap<>();
    private final Map<StyleTrait, Function<StyleDelegate<?>, Style>> _traitStylers = new LinkedHashMap<>();
    private final List<StyleTrait> _rootTraits = new java.util.ArrayList<>();
    private final List<List<StyleTrait>> _traitPaths = new java.util.ArrayList<>();

    private boolean _traitGraphBuilt = false;

    protected StyleSheet() {
        this( () -> UI.style() );
    }

    protected StyleSheet(Supplier<Style> defaultStyle) {
        _defaultStyle = defaultStyle;
        declaration();
        _buildTraitGraph();
    }

    protected StyleTrait<JComponent> id( String id ) { return new StyleTrait<>().id(id); }

    protected StyleTrait<JComponent> group( String name ) { return new StyleTrait<>().group(name); }

    protected <C extends JComponent> StyleTrait<C> type( Class<C> type ) { return new StyleTrait<>().type(type); }

    protected <C extends JComponent> void apply( StyleTrait<C> rule, Function<StyleDelegate<C>, Style> traitStyler ) {
        // First let's make sure the trait does not already exist.
        if ( _traitStylers.containsKey(rule) )
            throw new IllegalArgumentException("The trait " + rule.group() + " already exists in this style sheet.");

        // Now let's add the trait to the style sheet.
        _traitStylers.put(rule, (Function) traitStyler);
        // And let's add the trait to the trait graph.
        _traitGraph.put(rule, new java.util.ArrayList<>());
    }

    protected abstract void declaration();

    public Style run( JComponent toBeStyled ) {
        return run(toBeStyled, _defaultStyle.get());
    }

    public Style run( JComponent toBeStyled, Style startingStyle ) {
        if ( !_traitGraphBuilt )
            throw new IllegalStateException("The trait graph has not been built yet.");

        // Now we run the starting style through the trait graph.
        // We do this by finding valid trait paths from the root traits to the leaf traits.
        int deepestValidPath = -1;
        List<List<StyleTrait>> validTraitPaths = new java.util.ArrayList<>();
        for (List<StyleTrait> traitPath : _traitPaths) {
            List<String> inheritedStyleNames = new ArrayList<>();
            int lastValidTrait = -1;
            for (int i = 0; i < traitPath.size(); i++) {
                StyleTrait trait = traitPath.get(i);
                boolean valid = trait.isApplicableTo(toBeStyled, inheritedStyleNames);
                inheritedStyleNames.add(trait.group());
                if (valid) lastValidTrait = i;
            }
            if ( lastValidTrait >= 0 ) // We add the path up to the last valid trait to the list of valid traits.
                validTraitPaths.add(traitPath.subList(0, lastValidTrait + 1));

            if (lastValidTrait > deepestValidPath)
                deepestValidPath = lastValidTrait;
        }

        // Now we are going to create one common path from the valid trait paths by mergin them!
        // So first we add all the traits from path step 0, then 1, then 2, etc.
        List<StyleTrait> commonPath = new java.util.ArrayList<>();
        for ( int i = 0; i <= deepestValidPath; i++ ) {
            for ( List<StyleTrait> validTraitPath : validTraitPaths ) {
                if ( i < validTraitPath.size() ) {
                    StyleTrait trait = validTraitPath.get(i);
                    if ( !commonPath.contains(trait) )
                        commonPath.add(trait);
                }
            }
        }

        // Now we apply the valid traits to the starting style.
        for ( StyleTrait trait : commonPath )
            startingStyle = _traitStylers.get(trait).apply(new StyleDelegate<>(toBeStyled, startingStyle));

        return startingStyle;
    }

    private void _buildTraitGraph() {
        if ( _traitGraphBuilt )
            throw new IllegalStateException("The trait graph has already been built.");
        /*
            We compare each trait to every other trait to see if it inherits from it.
            If it does, we add the trait to the other trait's list of extension (the value in the map).
        */
        for ( StyleTrait trait : _traitGraph.keySet() )
            for ( StyleTrait other : _traitGraph.keySet() )
                if ( trait != other && trait.thisInherits(other) )
                    _traitGraph.get(other).add(trait);

        /*
            Now we have a graph of traits that inherit from other traits.
            We can use this graph to determine the order in which we apply the traits to a style.
            But first we need to make sure there are no cycles in the graph.

            We do this by performing a depth-first search on the graph.
        */
        for ( StyleTrait trait : _traitGraph.keySet() ) {
            // We create a stack onto which we will push the traits we visit and then pop them off.
            List<StyleTrait> visited = new java.util.ArrayList<>();
            // We pop the trait off the stack when we return from the recursive call.
            _dfs(trait, visited);
        }
        _findRootAndLeaveTraits();
        _traitGraphBuilt = true;
    }

    private void _dfs( StyleTrait current, List<StyleTrait> visited ) {
        // If the current trait is already in the visited list, then we have a cycle.
        if ( visited.contains(current) )
            throw new IllegalStateException("The style sheet contains a cycle.");

        // We add the current trait to the visited list.
        visited.add(current);

        // We recursively call the dfs method on each of the current trait's extensions.
        for ( StyleTrait extension : _traitGraph.get(current) )
            _dfs(extension, visited);

        // We remove the current trait from the visited list.
        visited.remove(current);
    }

    private void _findRootAndLeaveTraits() {
        /*
            We find the root traits by finding the traits, which have extensions,
            but are not referenced by any other trait as an extension.
         */
        for ( StyleTrait trait : _traitGraph.keySet() ) {
            boolean isLeaf = true;
            for ( StyleTrait other : _traitGraph.keySet() )
                if ( _traitGraph.get(other).contains(trait) ) {
                    isLeaf = false;
                    break;
                }
            if ( isLeaf )
                _rootTraits.add(trait);
        }
        /*
            Finally we can calculate all the possible paths from the root traits to the leaf traits.
        */
        _traitPaths.addAll(_findPaths());
    }

    private List<List<StyleTrait>> _findPaths() {
        List<List<StyleTrait>> paths = new java.util.ArrayList<>();
        for ( StyleTrait root : _rootTraits ) {
            List<StyleTrait> stack = new java.util.ArrayList<>();
            _traverse(root, paths, stack);
        }
        return paths;
    }

    private void _traverse(
        StyleTrait current,
        List<List<StyleTrait>> paths,
        List<StyleTrait> stack
    ) {
        stack.add(current);
        if ( _traitGraph.get(current).isEmpty() ) {
            List<List<StyleTrait>> newPath = Collections.singletonList(new ArrayList<>(stack));
            // We remove the last trait from the stack.
            stack.remove(stack.size() - 1);
            paths.addAll(newPath);
            return;
        }

        for ( StyleTrait extension : _traitGraph.get(current) )
            if ( extension != current )
                _traverse(extension, paths, stack);

        // We remove the last trait from the stack.
        stack.remove(stack.size() - 1);
    }

}