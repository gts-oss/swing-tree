package com.globaltcad.swingtree.examples.slim


import javax.swing.*
import java.awt.*

import static com.globaltcad.swingtree.UI.*

// We import the "UI" class from the "swingtree" package statically:

// This allows us to ommit the "UI." prefix when using the methods of the "UI" class.

class TodoApp extends JPanel
{
    TodoApp() {
        of(this).withLayout(FILL)
        .add(GROW & SPAN & WRAP, label("Todo List"))
        .add(GROW & SPAN & WRAP,
            panel(FILL).add(SHRINK, button("Add"), button("Remove"))
        )
        .add(
            GROW & SPAN & WRAP,
            panel()
            .apply({
                var myList = ["Get up in the morning", "Make coffee", "Drink coffee", "Code something in swingtree"]
                myList.eachWithIndex { task, i ->
                    it.add( WRAP,
                        panel().id("task-$i")
                        .add(SHRINK_X, label("Task $i:"))
                        .add(PUSH_X, checkBox(task))
                    )
                }
            })
        )
    }

    // Use this to test the above UI.
    public static void main(String... args) {
        new TestWindow(JFrame::new,new TodoApp()).getFrame().setSize(new Dimension(240, 325));
    }

}
