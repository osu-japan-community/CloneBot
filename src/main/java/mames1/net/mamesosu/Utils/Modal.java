package mames1.net.mamesosu.Utils;

import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public abstract class Modal {

     public static TextInput createTextInput(String id, String label, String description, boolean isRequire, TextInputStyle style) {

        return TextInput.create(id, label, style)
                .setMinLength(1)
                .setPlaceholder(description)
                .setRequired(isRequire)
                .build();
    }
}
