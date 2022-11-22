package example;

import com.globaltcad.swingtree.UI;

import javax.swing.*;

import static com.globaltcad.swingtree.UI.*;

public class SomeSpinnersView extends JPanel {

	public SomeSpinnersView(SomeSpinnersViewModel vm) {
		of(this).withLayout(FILL.and(INS(0)).and(DEBUG))
		.withPreferredSize(320, 100)
		.add(GROW,
			panel(FILL.and(INS(0)).and(WRAP(1)))
			.add(GROW, label("Base Size:"))
			.add(GROW,
				comboBox(vm.getBaseSize())
				.withRenderer(
					renderComboItem(SomeSpinnersViewModel.BaseSize.class)
					.asText( cell -> cell.getValue().title())
				)
			)
		)
		.add(GROW.and(PUSH_Y),
			panel(FILL.and(INS(0)).and(DEBUG))
			.add(GROW.and(WRAP),
				panel(FILL.and(INS(0)))
				.add(WIDTH(30, 60, 75).and(GROW).and(PUSH_Y),
					spinner(vm.getX())
				)
				.add(WIDTH(10, 12, 15), label("x"))
				.add(WIDTH(30, 52, 75).and(GROW).and(PUSH_Y),
					spinner(vm.getPercent())
				)
				.add(WIDTH(10, 60, 15), label("%"))
			)
			.add(GROW,
				panel(FILL.and(INS((3))).and(DEBUG))
				.add(PUSH_Y, panel())
				.add(PUSH_Y.and(GAP_LEFT_PUSH),
					button("-")
				)
				.add(ALIGN_CENTER,
					button("o")
				)
				.add(PUSH_Y.and(GAP_RIGHT_PUSH),
					button("+")
				)
				.add(PUSH_Y, panel())
			)
		);
	}

	public static void main(String... args) {
		UI.show(new SomeSpinnersView(new SomeSpinnersViewModel()));
	}

}

