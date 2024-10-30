/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectos.analizadorsql;

/**
 *
 * @author Windows 10
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Main {

    public static void main(String[] args) {
        // Crear la ventana principal
        JFrame frame = new JFrame("SQL Analyzer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400); // Aumentar el tamaño para acomodar dos áreas de texto

        // Crear un panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Crear un subpanel para las dos JTextArea
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(1, 2)); // Dividir el subpanel en dos columnas

        // Crear JTextArea para entrada de código SQL
        JTextArea textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        textPanel.add(scrollPane); // Añadir el área de entrada de SQL al subpanel

        // Crear JTextPane para mostrar los tokens
        JTextPane tokenTextPane = new JTextPane();
        tokenTextPane.setEditable(false); // Hacerlo solo de lectura
        JScrollPane tokenScrollPane = new JScrollPane(tokenTextPane);
        textPanel.add(tokenScrollPane); // Añadir el área de tokens al subpanel

        // Añadir el subpanel al panel principal
        panel.add(textPanel, BorderLayout.CENTER);

        
        // Añadir el panel principal a la ventana
        frame.add(panel);
        frame.setVisible(true);

        // Agregar un DocumentListener para cambiar el color en tiempo real
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processText(textArea, tokenTextPane);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                processText(textArea, tokenTextPane);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                processText(textArea, tokenTextPane);
            }
        });
    }

    // Método que procesa el texto ingresado y actualiza el JTextPane con los tokens coloreados
    private static void processText(JTextArea textArea, JTextPane tokenTextPane) {
        // Obtener el texto del JTextArea
        String inputText = textArea.getText();
        if (!inputText.isEmpty()) {
            // Crear un lexer con el texto ingresado
            SQLLexer lexer = new SQLLexer(new StringReader(inputText));
            processTokens(lexer, tokenTextPane); 
        } else {
            tokenTextPane.setText(""); // Limpiar el JTextPane si no hay texto
        }
    }

    // Método para procesar los tokens y asignarles color
    private static void processTokens(SQLLexer lexer, JTextPane tokenTextPane) {
        // Limpiar el JTextPane de tokens antes de procesar nuevos
        tokenTextPane.setText("");

        StyledDocument doc = tokenTextPane.getStyledDocument();

        Token token; // Declarar el token
        List<Token> tokenList = new ArrayList<>(); 

        try {
            while ((token = lexer.yylex()) != null) { // Obtener el token y verificar si es null
                // Obtener el texto y el color del token
                String tokenText = token.getText(); // Texto del token
                String tokenColorStr = token.getColor(); // Color del token (en formato String)

                // Verifica si ambos son válidos
                if (tokenText != null && tokenColorStr != null) {
                    Color tokenColor = getColorFromString(tokenColorStr); // Convertir cadena a Color

                    // Crear un nuevo estilo para cada token
                    Style style = tokenTextPane.addStyle("TokenStyle" + doc.getLength(), null);
                    StyleConstants.setForeground(style, tokenColor);

                    // Insertar el texto del token con el estilo en el JTextPane
                    try {
                        doc.insertString(doc.getLength(), tokenText + " ", style);
                    } catch (BadLocationException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    tokenList.add(token);
                } else {
                    System.err.println("Token nulo o sin color: " + token);
                }           
            }           
        } catch (Exception e) {
            System.err.println("Error durante el análisis léxico: " + e.getMessage());
        }

        // Instanciar el analizador sintáctico
        AnalizadorSintactico analizadorSintactico = new AnalizadorSintactico(tokenList);
        try { 
            analizadorSintactico.analizar();
            analizadorSintactico.generarArchivoHTML("Reportes.html",tokenList);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Método para convertir un string de color a un objeto Color
    private static Color getColorFromString(String colorStr) {
        try {
            // Verificar si es un color en formato hexadecimal
            if (colorStr.startsWith("#")) {
                return Color.decode(colorStr); // Convertir el string hexadecimal a Color
            } else {
                // Convertir nombres de colores predefinidos
                switch (colorStr.toLowerCase()) {
                    case "negro": return Color.BLACK;
                    case "azul": return Color.BLUE;
                    case "verde": return Color.GREEN;
                    case "gris": return Color.GRAY;
                    case "naranja": return Color.ORANGE;
                    case "fucsia": return Color.MAGENTA;
                    case "morado": return new Color(128, 0, 128); // Color morado (RGB)
                    case "amarillo": return Color.YELLOW;
                    default: return Color.BLACK; // Color por defecto si no se reconoce
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Error al convertir color: " + colorStr);
            return Color.BLACK; // Color por defecto si hay error
        }
    }
}
