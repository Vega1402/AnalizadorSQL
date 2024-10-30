/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectos.analizadorsql;

/**
 *
 * @author Windows 10
 */
public class Token {
    private String text;
    private String type;
    private String color;  // Nuevo atributo para el color

    public Token(String text, String type, String color) {
        this.text = text;
        this.type = type;
        this.color = color;  // Asignar el color
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public String getColor() {
        return color;  // Getter para el color
    }
    
        @Override
    public String toString() {
        return "" + text + "', type='" + type + "', color='" + color + "'}";
    }
}

