
# SwingTree [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![Java Version](https://img.shields.io/static/v1.svg?label=Java&message=8%2B&color=blue) #
## Modern Declarative UI Design for Swing ##

SwingTree is a Swing based UI framework for creating boilerplate free 
and composition based Swing UIs fluently. <br>
Think [Jetpack Compose](https://developer.android.com/jetpack/compose), [SwiftUI](https://developer.apple.com/xcode/swiftui/) or [Flutter](https://flutter.dev) but for Swing 
(similar to [JetBrains UI DSL](https://plugins.jetbrains.com/docs/intellij/kotlin-ui-dsl-version-2.html#ui-dsl-basics)).

- [Motivation](docs/markdown/Motivation.md)
- [Starter Tutorial](docs/markdown/Climbing-Swing-Tree.md)
- [Living Documentation](https://globaltcad.github.io/swing-tree/)

<table>
<tr>
<th></th>
<th></th>
</tr>
<tr>
<td> 

- lightweight, intuitive nested UI building in an HTML like fashion
- powerful layout declaration through `MigLayout`
- lambda friendly API for peeking into the underlying UI tree and manipulating swing components directly
- boilerplate free event registration through `onClick`, `onChange` methods...
- compatible with custom swing components and legacy Swing code
- built-in [MVVM property](https://github.com/globaltcad/sprouts) support for dynamic [UI design decoupled from business logic](docs/markdown/Advanced-MVVM.md) 
- advanced styling through a CSS like DSL API
	
</td>
<td>
	
<img href="https://www.flaticon.com/free-icons/swing" title="swing icons" src="docs/img/swing.png" style="width:200px;"/>
</td>
</tr>
</table>


---

Here an example of a simple calculator UI based on the `FlatLaF` look-and-feel:

<img href="" title="example" src="docs/img/simple-example.png" style="float:right;width:200px;margin:0.5em;"/>

This was made using the following code:

```java
FlatLightLaf.setup();
UI.of(this/*JPanel subtype*/).withLayout("fill, insets 10")
.add("grow, span, wrap",
   UI.panel("fill, ins 0")
   .add("shrink", UI.label("Result:"))
   .add("grow, wrap",
      UI.label("42.0").with(UI.HorizontalAlignment.RIGHT)
      .withProperty("FlatLaf.styleClass", "large")
   )
   .add("grow, span, wrap", UI.textArea(UI.HorizontalDirection.RIGHT_TO_LEFT, "13 - 73"))
)
.add("growx", UI.radioButton("DEG"), UI.radioButton("RAD"))
.add("shrinkx", UI.splitButton("sin"))
.add("growx, wrap", UI.button("Help").withProperty("JButton.buttonType", "help"))
.add("growx, span, wrap",
   UI.panel("fill")
   .add("span, grow, wrap",
       UI.panel("fill, ins 0")
       .add("grow",
           UI.button("(").withProperty("JButton.buttonType", "roundRect"),
           UI.button(")").withProperty("JButton.buttonType", "roundRect")
       )
   )
   .add("grow",
      UI.panel("fill, ins 0, wrap 3")
      .apply( it -> {
         String[] labels = {"7","8","9","4","5","6","1","2","3","0",".","C"};
         for ( var l : labels ) it.add("grow", UI.button(l));
      }),
      UI.panel("fill, ins 0")
      .add("grow", UI.button("-").withProperty("JButton.buttonType", "roundRect"))
      .add("grow, wrap", UI.button("/").withProperty("JButton.buttonType", "roundRect"))
      .add("span, grow, wrap",
         UI.panel("fill, ins 0")
         .add("grow", 
            UI.button("+").withProperty("JButton.buttonType", "roundRect"),
            UI.panel("fill, ins 0")
            .add("grow, wrap",
               UI.button("*").withProperty("JButton.buttonType", "roundRect"),
               UI.button("%").withProperty("JButton.buttonType", "roundRect")
            )
         ),
         UI.button("=")
         .withBackground(new Color(103, 255, 190))
         .withProperty("JButton.buttonType", "roundRect")
      )
   )
);
```


As you can see, swing tree has a very simple API, which only requires a
single class to be imported, the `UI` class which can even be imported
statically to remove any `UI.` prefixes.

Also, note that there are usually 2 arguments
added to a tree node: a `String` and then UI nodes.
This first argument simply translates
to the layout constraints which should be applied
to the UI element(s) added.

In this example, strings will be passed to a `MigLayout`
simply because it is a general purpose layout and no other
layout was specified.

For more examples take a look at the <a href="src/test/groovy/swingtree/examples">examples folder</a> inside the test suite.


---
## Getting started with Apache Maven ##

```
<dependency>
  <groupId>io.github.globaltcad</groupId>
  <artifactId>swing-tree</artifactId>
  <version>0.3.0</version>
</dependency>
```

---

## Getting started with Gradle ##
Groovy DSL:
```
implementation 'io.github.globaltcad:swing-tree:0.3.0'
```
Kotlin DSL:
```
implementation("io.github.globaltcad:swing-tree:0.3.0")
```
---

## Getting started with [![](https://jitpack.io/v/globaltcad/swing-tree.svg)](https://jitpack.io/#globaltcad/swing-tree) ##
**1. Add the JitPack url in your root `build.gradle` at the end of `repositories`**
```
allprojects {
	repositories {
		//...
		maven { url 'https://jitpack.io' }
	}
}
```
**2. Add swing-tree as dependency**

...either by specifiying the version tag:
```
dependencies {
	implementation 'com.github.globaltcad:swing-tree:v0.0.1'
}
```
...or by using a custom commit hash instead:
```
dependencies {
	implementation 'com.github.globaltcad:swing-tree:7c74811'//Any commit hash...
}
```
---

