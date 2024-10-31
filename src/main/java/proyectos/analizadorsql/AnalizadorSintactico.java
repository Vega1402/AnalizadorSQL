/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectos.analizadorsql;

/**
 *
 * @author Windows 10
 */




import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;


public class AnalizadorSintactico {

    private Iterator<Token> tokens;
    private Token tokenActual;
    private Set<String> mensajesDeError; 
    private List<String> mensajesDeExito = new ArrayList<>();
     
    // Constructor para inicializar la lista de tokens
    public AnalizadorSintactico(List<Token> tokens) {
        this.tokens = tokens.iterator();

        avanzar(); // Avanza al primer token
        mensajesDeError = new HashSet<>(); // Inicializa el conjunto de mensajes de error

    }

    // Método para avanzar al siguiente token
    private void avanzar() {
        if (tokens.hasNext()) {
            tokenActual = tokens.next();
        } else {
            tokenActual = null; // Indica el fin de los tokens
        }
    }

    
    
    public void analizar() {
    while (tokenActual != null) {
        // Manejo para CREATE
        if (tokenActual.getText().equals("CREATE")) {
            avanzar();
            if (tokenActual.getText().equals("DATABASE")) {
                analizarCrearBaseDeDatos();
               continue;
            }
            if (tokenActual.getText().equals("TABLE")) {
                analizarCrearTabla();
                continue;
            }
            // Error para CREATE sin DATABASE o TABLE
            registrarError("Se esperaba 'DATABASE' o 'TABLE' después de 'CREATE'");
            avanzar();
            continue;
        }
        
        // Manejo para INSERT
        if (tokenActual.getText().equals("INSERT")) {
            avanzar();
            if (tokenActual.getText().equals("INTO")) {
                analizarInsercion();
                continue;
            }
            // Error para INSERT sin INTO
            registrarError("Se esperaba 'INTO' después de 'INSERT'");
            avanzar();
            continue;
        }
        
        
        //ALTER TABLA

        if (tokenActual.getText().equals("ALTER")) {
                analizarAlterTable();
                continue;
            }
        
        //Update y Delete
        if (tokenActual.getText().equals("SELECT")) {
                analizarSelect();
                continue;
            }
        
        if (tokenActual.getText().equals("UPDATE")) {
                avanzar();
                analizarUpdate();
                continue;
            }

            // Manejo para DELETE
            if (tokenActual.getText().equals("DETELE")) {
                avanzar();
                analizarDelete();
                continue;
            }            
        avanzar(); // Avanza al siguiente token
    }
}




    // Método para analizar la estructura CREATE DATABASE
    private void analizarCrearBaseDeDatos() {
        avanzar();
        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
            String nombreIdentificador = tokenActual.getText();
            avanzar();
            if (tokenActual != null && tokenActual.getText().equals(";")) {
                avanzar();
                registrarMensajeExito("CREATE DATABASE: "+ nombreIdentificador  );
            } else {
                registrarError("Se esperaba ';' al final de la instrucción CREATE DATABASE");
            }
        } else {
            registrarError("Se esperaba un identificador después de 'CREATE DATABASE'");
        }
    }

    // Método para analizar la estructura CREATE TABLE
    private void analizarCrearTabla() {
        avanzar();
        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
            String nombreIdentificador = tokenActual.getText();
            avanzar();
            if (tokenActual != null && tokenActual.getText().equals("(")) {
                avanzar();
                analizarEstructuraTabla();
                if (tokenActual != null && tokenActual.getText().equals(")")) {
                    avanzar();
                    if (tokenActual != null && tokenActual.getText().equals(";")) {
                        avanzar();
                        registrarMensajeExito("CREATE TABLE: "+ nombreIdentificador  );
                    } else {
                        registrarError("Se esperaba ';' al final de la instrucción CREATE TABLE");
                    }
                } else {
                    registrarError("Se esperaba ')' para cerrar la declaración de la tabla");
                }
            } else {
                registrarError("Se esperaba '(' después del nombre de la tabla");
            }
        } else {
            registrarError("Se esperaba un identificador para el nombre de la tabla");
        }
    }

    // Método para analizar las declaraciones de columnas y llaves
    private void analizarEstructuraTabla() {
        while (tokenActual != null && !tokenActual.getText().equals(")")) {
            if (tokenActual.getText().equals("CONSTRAINT")) {
                analizarEstructuraDeLlaves();
            } else {
                analizarEstructuraDeclaracion();
            }
            if (tokenActual != null && tokenActual.getText().equals(",")) {
                avanzar(); // Avanza después de cada coma entre declaraciones
            } else {
                break;
            }
        }
    }

    // Método para analizar una declaración de columna
    private void analizarEstructuraDeclaracion() {
        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
            avanzar();
            analizarTipoDeDato();
            if (tokenActual != null && tokenActual.getText().equals("PRIMARY")) {
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals("KEY")) {
                    avanzar();
                } else {
                    registrarError("Se esperaba 'KEY' después de 'PRIMARY'");
                }
            } else if (tokenActual != null && tokenActual.getText().equals("NOT")) {
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals("NULL")) {
                    avanzar();
                } else {
                    registrarError("Se esperaba 'NULL' después de 'NOT'");
                }
            } else if (tokenActual != null && tokenActual.getText().equals("UNIQUE")) {
                avanzar();
            }
        } else {
            registrarError("Se esperaba un identificador para el nombre de la columna");
        }
    }

    // Método para analizar los tipos de datos permitidos
    private void analizarTipoDeDato() {
        if (tokenActual != null) {
            switch (tokenActual.getText()) {
                case "SERIAL":
                case "INTEGER":
                case "BIGINT":
                case "DATE":
                case "TEXT":
                case "BOOLEAN":
                    avanzar();
                    break;
                case "VARCHAR":
                    avanzar();
                    if (tokenActual != null && tokenActual.getText().equals("(")) {
                        avanzar();
                        if (tokenActual != null && tokenActual.getType().equals("Entero")) {
                            avanzar();
                            if (tokenActual != null && tokenActual.getText().equals(")")) {
                                avanzar();
                            } else {
                                registrarError("Se esperaba ')' después del tamaño de VARCHAR");
                            }
                        } else {
                            registrarError("Se esperaba un entero para el tamaño de VARCHAR");
                        }
                    } else {
                        registrarError("Se esperaba '(' después de 'VARCHAR'");
                    }
                    break;
                case "DECIMAL":
                    avanzar();
                    if (tokenActual != null && tokenActual.getText().equals("(")) {
                        avanzar();
                        if (tokenActual != null && tokenActual.getType().equals("Entero")) {
                            avanzar();
                            if (tokenActual != null && tokenActual.getText().equals(",")) {
                                avanzar();
                                if (tokenActual != null && tokenActual.getType().equals("Entero")) {
                                    avanzar();
                                    if (tokenActual != null && tokenActual.getText().equals(")")) {
                                        avanzar();
                                    } else {
                                        registrarError("Se esperaba ')' después de la precisión y escala de DECIMAL");
                                    }
                                } else {
                                    registrarError("Se esperaba un entero para la escala de DECIMAL");
                                }
                            } else {
                                registrarError("Se esperaba ',' después de la precisión de DECIMAL");
                            }
                        } else {
                            registrarError("Se esperaba un entero para la precisión de DECIMAL");
                        }
                    } else {
                        registrarError("Se esperaba '(' después de 'DECIMAL'");
                    }
                    break;
                default:
                    registrarError("Tipo de dato no válido");
            }
        }
    }

    // Método para analizar la estructura de llaves (CONSTRAINT FOREIGN KEY)
    private void analizarEstructuraDeLlaves() {
        avanzar();
        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
            avanzar();
            if (tokenActual != null && tokenActual.getText().equals("FOREIGN")) {
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals("KEY")) {
                    avanzar();
                    if (tokenActual != null && tokenActual.getText().equals("(")) {
                        avanzar();
                        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                            avanzar();
                            if (tokenActual != null && tokenActual.getText().equals(")")) {
                                avanzar();
                                if (tokenActual != null && tokenActual.getText().equals("REFERENCES")) {
                                    avanzar();
                                    if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                                        avanzar();
                                        if (tokenActual != null && tokenActual.getText().equals("(")) {
                                            avanzar();
                                            if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                                                avanzar();
                                                if (tokenActual != null && tokenActual.getText().equals(")")) {
                                                    avanzar();
                                                } else {
                                                    registrarError("Se esperaba ')' después del identificador de columna en REFERENCES");
                                                }
                                            } else {
                                                registrarError("Se esperaba un identificador para el nombre de la columna en REFERENCES");
                                            }
                                        } else {
                                            registrarError("Se esperaba '(' después del nombre de la tabla en REFERENCES");
                                        }
                                    } else {
                                        registrarError("Se esperaba un identificador para el nombre de la tabla en REFERENCES");
                                    }
                                } else {
                                    registrarError("Se esperaba 'REFERENCES' después de la declaración de la llave foránea");
                                }
                            } else {
                                registrarError("Se esperaba ')' para cerrar la declaración de la llave foránea");
                            }
                        } else {
                            registrarError("Se esperaba un identificador para el nombre de la columna de la llave foránea");
                        }
                    } else {
                        registrarError("Se esperaba '(' después de 'FOREIGN KEY'");
                    }
                } else {
                    registrarError("Se esperaba 'KEY' después de 'FOREIGN'");
                }
            } else {
                registrarError("Se esperaba 'FOREIGN' después de 'CONSTRAINT'");
            }
        } else {
            registrarError("Se esperaba un identificador para el nombre de la restricción");
        }
    }

    // Método para registrar errores únicos
    private void registrarError(String mensaje) {
        if (mensajesDeError.add(mensaje)) { // Solo se añade si no está presente
        //    System.out.println("Error: " + mensaje);
        }
    }
    
    // Método para registrar un único mensaje de éxito
    private void registrarMensajeExito(String mensaje) {
        mensajesDeExito.add(mensaje); 
    }
    // Método para generar un archivo HTML con los errores y los tokens
public void generarArchivoHTML(String nombreArchivo, List<Token> tokens) {
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nombreArchivo), "UTF-8"))) {
        // Escribiendo la cabecera del documento HTML
        writer.write("<!DOCTYPE html>");
        writer.write("<html lang=\"es\">");
        writer.write("<head>");
        writer.write("<meta charset=\"UTF-8\">");
        writer.write("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        writer.write("<title>Reporte de Errores y Tokens</title>");
        writer.write("<style>");
        writer.write("body { font-family: Arial, sans-serif; margin: 20px; }");
        writer.write("h1 { color: #333; }");
        writer.write("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
        writer.write("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        writer.write("th { background-color: #f2f2f2; }");
        writer.write(".error { color: red; }");
        writer.write(".valido { color: green; font-weight: bold; }");
        writer.write("</style>");
        writer.write("</head>");
        writer.write("<body>");
        writer.write("<h1>Reporte de Errores y Tokens</h1>");

        // Verifica si hay errores para mostrar
        if (mensajesDeError.isEmpty()) {
            writer.write("<p>No se encontraron errores.</p>");
        } else {
            writer.write("<h2>Errores Encontrados</h2>");
            writer.write("<table>");
            writer.write("<tr><th>Errores</th></tr>");

            // Escribiendo cada error en la tabla
            for (String mensaje : mensajesDeError) {
                writer.write("<tr><td class=\"error\">" + escapeHtml(mensaje) + "</td></tr>");
            }

            writer.write("</table>");
        }
        
        // Verifica si hay estructuras para mostrar
        if (mensajesDeExito.isEmpty()) {
            writer.write("<p>No se encontraron estructuras válidas.</p>");
        } else {
            writer.write("<h2>Estructuras Encontradas</h2>");
            writer.write("<table>");
            writer.write("<tr><th>Estructuras</th></tr>");

            // Escribiendo cada mensaje de éxito en la tabla
            for (String mensaje : mensajesDeExito) {
                writer.write("<tr><td>" + escapeHtml(mensaje) + 
                             " <span class='valido'>Estructura válida</span></td></tr>");
            }

            writer.write("</table>");
        }

        // Generar sección de tokens
        writer.write("<h2>Tokens Encontrados</h2>");
        writer.write("<table>");
        writer.write("<tr><th>Texto</th><th>Tipo</th><th>Color</th></tr>");

        // Escribiendo cada token en la tabla
        for (Token token : tokens) {
            writer.write("<tr>");
            writer.write("<td>" + escapeHtml(token.getText()) + "</td>");
            writer.write("<td>" + escapeHtml(token.getType()) + "</td>");
            writer.write("<td style=\"color: " + token.getColor() + ";\">" + escapeHtml(token.getColor()) + "</td>");
            writer.write("</tr>");
        }

        writer.write("</table>");
        writer.write("</body>");
        writer.write("</html>");
    } catch (IOException e) {
        System.err.println("Error al generar el archivo HTML: " + e.getMessage());
    }
}

// Método para escapar caracteres HTML
private String escapeHtml(String texto) {
    return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
}

    
    // Agregar esta parte en la clase AnalizadorSintactico para analizar la instrucción INSERT INTO

// Método para analizar la estructura INSERT INTO
private void analizarInsercion() {
    avanzar();
    if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
        String nombreIdentificador = tokenActual.getText();
        avanzar();
        if (tokenActual != null && tokenActual.getText().equals("(")) {
            avanzar();
            analizarIdentificadoresDeColumnas();
            if (tokenActual != null && tokenActual.getText().equals(")")) {
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals("VALUES")) {
                    avanzar();
                    analizarValores(); 
                    registrarMensajeExito("INSERT INTO: "+ nombreIdentificador  );
                } else {
                    registrarError("Se esperaba 'VALUES' después de la lista de columnas en 'INSERT INTO'");
                }
            } else {
                registrarError("Se esperaba ')' para cerrar la lista de columnas");
            }
        } else {
            registrarError("Se esperaba '(' después del nombre de la tabla en 'INSERT INTO'");
        }
    } else {
        registrarError("Se esperaba un identificador para el nombre de la tabla en 'INSERT INTO'");
    }
}

// Método para analizar la lista de identificadores de columnas
private void analizarIdentificadoresDeColumnas() {
    while (tokenActual != null && tokenActual.getType().equals("Identificador")) {
        avanzar();
        if (tokenActual != null && tokenActual.getText().equals(",")) {
            avanzar();
        } else {
            break;
        }
    }
}

// Método para analizar la lista de valores asociados
private void analizarValores() {
    if (tokenActual != null && tokenActual.getText().equals("(")) {
        do {
            avanzar();
            analizarDato();
            while (tokenActual != null && tokenActual.getText().equals(",")) {
                avanzar();
                analizarDato();
            }
            if (tokenActual != null && tokenActual.getText().equals(")")) {
                avanzar();
            } else {
                registrarError("Se esperaba ')' para cerrar la lista de valores");
            }
        } while (tokenActual != null && tokenActual.getText().equals(",") && tokenActual.getText().equals("("));
    } else {
        registrarError("Se esperaba '(' después de 'VALUES'");
    }
}

// Método para analizar un dato en la lista de valores
private void analizarDato() {
    if (tokenActual != null) {
        switch (tokenActual.getType()) {
            case "Entero":
            case "Decimal":
            case "Fecha":
            case "Cadena":
            case "Booleano":
                avanzar();
                if (tokenActual != null && (tokenActual.getType().equals("Operador Aritmético") || tokenActual.getType().equals("Operador Relacional"))) {
                    avanzar(); // Avanza sobre el operador
                    analizarDato(); // Vuelve a analizar para el siguiente valor después del operador
                }
                break;
            case "ParentesisApertura":
                analizarExpresionConParentesis();
                break;
            default:
                registrarError("Se esperaba un dato válido en la lista de valores");
                break;
        }
    }
}


private void analizarExpresionConParentesis() {
    if (tokenActual != null && tokenActual.getText().equals("(")) {
        avanzar();
        analizarDato();
        while (tokenActual != null && (tokenActual.getType().equals("Operador Aritmético") || tokenActual.getType().equals("Operador Relacional"))) {
            avanzar(); // Avanza sobre el operador
            analizarDato(); // Analiza el valor o dato siguiente en la expresión
        }
        if (tokenActual != null && tokenActual.getText().equals(")")) {
            avanzar();
        } else {
            registrarError("Se esperaba ')' al final de la expresión entre paréntesis");
        }
    }
}

//Código del Analizador Sintáctico para Consultas SELECT



     // Método para analizar la estructura SELECT
    private void analizarSelect() {
        avanzar();
        analizarSeleccionDeColumnas();

        if (tokenActual != null && tokenActual.getText().equals("FROM")) {
            avanzar();
            if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                String nombreIdentificador = tokenActual.getText();
                avanzar();
                analizarSentenciasOpcionales();
                registrarMensajeExito("SELECT FROM: "+ nombreIdentificador  );
            } else {
                registrarError("Se esperaba un identificador después de 'FROM'");
            }
        } else {
            registrarError("Se esperaba 'FROM' después de 'SELECT'");
        }
    }
    


                

    // Método para analizar la selección de columnas
    private void analizarSeleccionDeColumnas() {
        if (tokenActual.getText().equals("*")) {
            avanzar();
        } else {
            while (tokenActual != null) {
                if (tokenActual.getType().equals("Identificador")) {
                    avanzar();
                    if (tokenActual != null && tokenActual.getText().equals(".")) {
                        avanzar();
                        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                            avanzar();
                        } else {
                            registrarError("Se esperaba un identificador después de '.' en la selección de columnas");
                        }
                    }
                } else if (esFuncionAgregacion()) {
                    analizarFuncionAgregacion();
                } else {
                    registrarError("Se esperaba '*', una función de agregación o un identificador en la selección de columnas");
                }

                if (tokenActual != null && tokenActual.getText().equals(",")) {
                    avanzar();
                } else {
                    break;
                }
            }
        }
    }

    // Método para verificar y analizar las funciones de agregación
    private boolean esFuncionAgregacion() {
        String texto = tokenActual.getText();
        return texto.equals("SUM") || texto.equals("AVG") || texto.equals("COUNT") || texto.equals("MIN") || texto.equals("MAX");
    }

    private void analizarFuncionAgregacion() {
        avanzar(); 
        if (tokenActual != null && tokenActual.getText().equals("(")) {
            avanzar();
            if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals(")")) {
                    avanzar();
                } else {
                    registrarError("Se esperaba ')' al final de la función de agregación");
                }
            } else {
                registrarError("Se esperaba un identificador dentro de la función de agregación");
            }
        } else {
            registrarError("Se esperaba '(' después del nombre de la función de agregación");
        }
    }

    // Método para analizar las sentencias opcionales
    private void analizarSentenciasOpcionales() {
        while (tokenActual != null) {
            switch (tokenActual.getText()) {
                case "JOIN":
                    analizarJoin();
                    break;
                case "WHERE":
                    analizarWhere();
                    break;
                case "GROUP":
                    analizarGroupBy();
                    break;
                case "ORDER":
                    analizarOrderBy();
                    break;
                case "LIMIT":
                    analizarLimit();
                    break;
                default:
                    return;
            }
        }
    }

    private void analizarJoin() {
        avanzar();
        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
            avanzar();
            if (tokenActual != null && tokenActual.getText().equals("ON")) {
                avanzar();
                analizarCondicionJoin();
            } else {
                registrarError("Se esperaba 'ON' después de la tabla en la cláusula JOIN");
            }
        } else {
            registrarError("Se esperaba un identificador después de 'JOIN'");
        }
    }

    private void analizarCondicionJoin() {
        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
            avanzar();
            if (tokenActual != null && tokenActual.getText().equals("=")) {
                avanzar();
                if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                    avanzar();
                } else {
                    registrarError("Se esperaba un identificador después de '=' en la cláusula JOIN");
                }
            } else {
                registrarError("Se esperaba '=' en la condición de JOIN");
            }
        } else {
            registrarError("Se esperaba un identificador en la condición de JOIN");
        }
    }

    private void analizarWhere() {
        avanzar();
        analizarCondicion();
    }

    private void analizarCondicion() {
    if (tokenActual != null && tokenActual.getText().equals("(")) {
        avanzar();
        analizarCondicion();  // Recursión para analizar una subcondición
        if (tokenActual != null && tokenActual.getText().equals(")")) {
            avanzar();
        } else {
            registrarError("Se esperaba ')' para cerrar la condición en WHERE");
            return;
        }
    } else if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
        avanzar();
        if (tokenActual != null && (tokenActual.getText().equals("=") || tokenActual.getText().matches("[<>]=?|<>"))) {
            avanzar();
            if (tokenActual != null && (tokenActual.getType().equals("Identificador") || tokenActual.getType().matches("Entero|Decimal|Cadena"))) {
                avanzar();
            } else {
                registrarError("Se esperaba un valor o identificador después del operador en la condición WHERE");
            }
        } else {
            registrarError("Operador de comparación esperado en la condición WHERE");
        }
    } else {
        registrarError("Se esperaba un identificador en la condición WHERE");
    }

    // Manejar operadores lógicos AND, OR
    if (tokenActual != null && (tokenActual.getText().equals("AND") || tokenActual.getText().equals("OR"))) {
        avanzar();
        analizarCondicion();  // Llamada recursiva para manejar expresiones compuestas
    }
}


    private void analizarGroupBy() {
        avanzar();
        if (tokenActual != null && tokenActual.getText().equals("BY")) {
            avanzar();
            analizarIdentificadorConPunto();
        } else {
            registrarError("Se esperaba 'BY' después de 'GROUP'");
        }
    }

    private void analizarOrderBy() {
        avanzar();
        if (tokenActual != null && tokenActual.getText().equals("BY")) {
            avanzar();
            analizarIdentificadorConPunto();
            if (tokenActual != null && (tokenActual.getText().equals("ASC") || tokenActual.getText().equals("DESC"))) {
                avanzar();
            }
        } else {
            registrarError("Se esperaba 'BY' después de 'ORDER'");
        }
    }

    private void analizarLimit() {
        avanzar();
        if (tokenActual != null && tokenActual.getType().equals("Entero")) {
            avanzar();
        } else {
            registrarError("Se esperaba un número entero después de 'LIMIT'");
        }
    }

    private void analizarIdentificadorConPunto() {
        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
            avanzar();
            if (tokenActual != null && tokenActual.getText().equals(".")) {
                avanzar();
                if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                    avanzar();
                } else {
                    registrarError("Se esperaba un identificador después de '.'");
                }
            }
        } else {
            registrarError("Se esperaba un identificador");
        }
    }
  
    
    //detele y update
    
private void analizarUpdate() {
    if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
        String nombreTabla = tokenActual.getText(); // Guarda el nombre de la tabla
        avanzar(); // Avanza al siguiente token

        if (tokenActual != null && tokenActual.getText().equals("SET")) {
            avanzar();
            analizarSet(); // Analiza la parte SET

            // Manejo opcional para WHERE
            if (tokenActual != null && tokenActual.getText().equals("WHERE")) {
                avanzar();
                analizarCondicionUp(); // Analiza la condición
                 registrarMensajeExito("UPDATE: "+ nombreTabla  );
            }
        } else {
            registrarError("Se esperaba 'SET' después del identificador de la tabla");
            return; // Termina el análisis si hay un error
        }

        // Verificación del final de la instrucción
        if (tokenActual != null && tokenActual.getText().equals(";")) {
            avanzar(); // Fin de la instrucción
        } else {
            registrarError("Se esperaba ';' al final de la instrucción UPDATE");
        }
    } else {
        registrarError("Se esperaba un identificador para el nombre de la tabla");
    }
}


private void analizarSet() {
    boolean primeraAsignacion = true; // Para controlar la primera asignación
    while (tokenActual != null && !tokenActual.getText().equals(";") && !tokenActual.getText().equals("WHERE")) {
        if (tokenActual.getType().equals("Identificador")) {
            String columna = tokenActual.getText(); // Guarda el nombre de la columna
            avanzar(); // Avanza al siguiente token

            if (tokenActual != null && tokenActual.getText().equals("=")) {
                avanzar(); // Avanza al token siguiente (el dato a asignar)
                analizarExpresionAritmetica(); // Analiza una expresión aritmética o un dato
                primeraAsignacion = false; // Marca que al menos una asignación se realizó
            } else {
                registrarError("Se esperaba '=' después del identificador de la columna");
                return; // Salida anticipada si hay un error
            }
        } else {
            registrarError("Se esperaba un identificador para el nombre de la columna");
            return; // Salida anticipada si hay un error
        }

        // Manejo de múltiples asignaciones
        if (tokenActual != null && tokenActual.getText().equals(",")) {
            avanzar(); // Avanza para más asignaciones
        } else {
            break; // Sale si no hay más asignaciones
        }
    }

    // Si no se realizó ninguna asignación, registrar un error
    if (primeraAsignacion) {
        registrarError("Se esperaba al menos una asignación en la parte SET");
    }
}


// Método para analizar una expresión aritmética simple (dato o dato + operador + dato)
private void analizarExpresionAritmetica() {
    analizarDatoUp(); // Analiza el primer operando

    // Mientras haya un operador aritmético, seguir analizando la expresión
    while (tokenActual != null && 
          (tokenActual.getType().equals("Operador Aritmético"))) {
        avanzar(); // Avanza al siguiente operador
        analizarDatoUp(); // Analiza el segundo operando o dato siguiente
    }
}

// Método para analizar un dato (puede ser un literal, variable, etc.)
private void analizarDatoUp() {
    if (tokenActual != null) {
        if (tokenActual.getType().equals("Decimal") || 
            tokenActual.getType().equals("Entero") || 
            tokenActual.getType().equals("Cadena") || 
            tokenActual.getType().equals("Identificador")) {
            avanzar(); // Avanza si es un dato válido
        } else {
            registrarError("Tipo de dato no válido: " + tokenActual.getText());
        }
    } else {
        registrarError("Se esperaba un dato a asignar");
    }
}

private void analizarCondicionUp() {
    boolean condicionValida = false; // Controla que se incluya una condición válida
    while (tokenActual != null && !tokenActual.getText().equals(";")) {
        if (tokenActual.getType().equals("Identificador")) {
            
            avanzar();
            if (tokenActual != null && (tokenActual.getType().equals("Operador Relacional")|| tokenActual.getType().equals("Signo"))) {
                avanzar();
                if (tokenActual != null && (tokenActual.getType().equals("Decimal") || 
                                            tokenActual.getType().equals("Entero") || 
                                            tokenActual.getType().equals("Cadena") || 
                                            tokenActual.getType().equals("Identificador"))) {
                    avanzar();
                    condicionValida = true;                   
                } else {
                    registrarError("Se esperaba un valor después del operador relacional en WHERE");
                    return;
                }
            } else {
                registrarError("Se esperaba un operador relacional en WHERE");
                return;
            }
        } else if (tokenActual.getType().equals("Operador Lógico")) {
            avanzar(); // Avanza después de un operador lógico
        } else {
            registrarError("Condición no válida en WHERE");
            return;
        }
    }

    if (!condicionValida) {
        registrarError("Se esperaba una condición válida en la cláusula WHERE");
    }
}



    
    
    // Método para analizar la estructura DELETE
    private void analizarDelete() {
        if (tokenActual != null && tokenActual.getText().equals("FROM")) {
            avanzar();
            if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                String nombreIdentificador = tokenActual.getText();
                avanzar(); // Identificador de la tabla
                // Manejo opcional para WHERE
                if (tokenActual != null && tokenActual.getText().equals("WHERE")) {
                    avanzar();
                    analizarCondicion();
                }
                if (tokenActual != null && tokenActual.getText().equals(";")) {                   
                    avanzar(); // Fin de la instrucción
                    registrarMensajeExito("DETELE: "+ nombreIdentificador  );
                } else {
                    registrarError("Se esperaba ';' al final de la instrucción DELETE");
                }
            } else {
                registrarError("Se esperaba un identificador para el nombre de la tabla en DELETE");
            }
        } else {
            registrarError("Se esperaba 'FROM' después de 'DELETE'");
        }
    }
    
    //alter table
    
    
    private void analizarAlterTable() {
        avanzar();
        if (tokenActual != null && tokenActual.getText().equals("TABLE")) {
            avanzar();
            if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                String nombreTabla = tokenActual.getText();
                avanzar();
                if (tokenActual != null) {
                    switch (tokenActual.getText()) {
                        case "ADD":
                            avanzar();
                            if (tokenActual != null && tokenActual.getText().equals("COLUMN")) {
                                analizarAddColumn(nombreTabla);
                            } else if (tokenActual != null && tokenActual.getText().equals("CONSTRAINT")) {
                                analizarAddConstraint(nombreTabla);
                            } else {
                                registrarError("Se esperaba 'COLUMN' o 'CONSTRAINT' después de 'ADD'");
                                avanzar();
                            }
                            break;
                        case "DROP":
                            analizarDropColumn(nombreTabla);
                            break;
                        case "ALTER":
                            analizarAlterColumn(nombreTabla);
                            break;
                        default:
                            registrarError("Se esperaba 'ADD', 'DROP', 'ALTER' o 'CONSTRAINT' después de 'TABLE'");
                            avanzar();
                            break;
                    }
                } registrarMensajeExito("ALTER TABLE: "+ nombreTabla  );
            } else {
                registrarError("Se esperaba un identificador para el nombre de la tabla");
            }
        } else {
            registrarError("Se esperaba 'TABLE' después de 'ALTER'");
        }
    }

    private void analizarAddColumn(String nombreTabla) {
        avanzar();
        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
            String nombreColumna = tokenActual.getText();
            avanzar();
            analizarTipoDeDatoAl();
            if (tokenActual != null && tokenActual.getText().equals(";")) {
                avanzar();
            } else {
                registrarError("Se esperaba ';' al final de la instrucción ADD COLUMN");
            }
        } else {
            registrarError("Se esperaba un identificador para el nombre de la columna");
        }
    }

    private void analizarAddConstraint(String nombreTabla) {
    avanzar();
    if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
        String nombreRestriccion = tokenActual.getText();
        avanzar();

        // Verificar si es una restricción de UNIQUE o FOREIGN KEY
        if (tokenActual != null && tokenActual.getType().equals("CREATE")) {
            avanzar();
            if (tokenActual != null && tokenActual.getText().equals("UNIQUE")) {
                // Manejar la restricción UNIQUE
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals("(")) {
                    avanzar();
                    if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                        avanzar();
                        if (tokenActual != null && tokenActual.getText().equals(")")) {
                            avanzar();
                            // Opcional: llamar a un método para manejar las opciones On Delete y On Update
                            // analizarOpcionesOnDeleteOnUpdate();
                            // Registrar el éxito o almacenar la restricción UNIQUE
                        } else {
                            registrarError("Se esperaba ')' después del identificador en UNIQUE");
                        }
                    } else {
                        registrarError("Se esperaba un identificador en UNIQUE");
                    }
                } else {
                    registrarError("Se esperaba '(' después de 'UNIQUE'");
                }
            } else if (tokenActual != null && tokenActual.getText().equals("FOREIGN")) {
                // Manejar la restricción FOREIGN KEY
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals("KEY")) {
                    avanzar();
                    if (tokenActual != null && tokenActual.getText().equals("(")) {
                        avanzar();
                        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                            avanzar();
                            if (tokenActual != null && tokenActual.getText().equals(")")) {
                                avanzar();
                                if (tokenActual != null && tokenActual.getText().equals("REFERENCES")) {
                                    avanzar();
                                    if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                                        avanzar();
                                        if (tokenActual != null && tokenActual.getText().equals("(")) {
                                            avanzar();
                                            if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                                                avanzar();
                                                if (tokenActual != null && tokenActual.getText().equals(")")) {
                                                    avanzar();
                                                    analizarOpcionesOnDeleteOnUpdate();
                                                   
                                                } else {
                                                    registrarError("Se esperaba ')' después del identificador en REFERENCES");
                                                }
                                            } else {
                                                registrarError("Se esperaba un identificador en REFERENCES");
                                            }
                                        } else {
                                            registrarError("Se esperaba '(' después de REFERENCES");
                                        }
                                    } else {
                                        registrarError("Se esperaba un identificador para la tabla referenciada en FOREIGN KEY");
                                    }
                                } else {
                                    registrarError("Se esperaba 'REFERENCES' en la definición de FOREIGN KEY");
                                }
                            } else {
                                registrarError("Se esperaba ')' después del identificador en FOREIGN KEY");
                            }
                        } else {
                            registrarError("Se esperaba un identificador para la columna en FOREIGN KEY");
                        }
                    } else {
                        registrarError("Se esperaba '(' después de 'FOREIGN KEY'");
                    }
                } else {
                    registrarError("Se esperaba 'KEY' después de 'FOREIGN'");
                }
            } else {
              //  registrarError("Tipo de restricción no reconocido después de 'ADD CONSTRAINT'");
            }
        } else {
            registrarError("Se esperaba 'CREATE' después del nombre de la restricción");
        }
    } else {
        registrarError("Se esperaba un identificador para el nombre de la restricción");
    }
}


    private void analizarOpcionesOnDeleteOnUpdate() {
        while (tokenActual != null && (tokenActual.getText().equals("ON"))) {
            avanzar();
            if (tokenActual != null && tokenActual.getText().equals("DELETE")) {
                avanzar();
                if (tokenActual != null && (tokenActual.getText().equals("SET") || tokenActual.getText().equals("CASCADE"))) {
                    avanzar();
                    if (tokenActual.getText().equals("NULL") || tokenActual.getText().equals("CASCADE")) {
                        avanzar();
                    } else {
                        registrarError("Se esperaba 'NULL' o 'CASCADE' después de 'SET' en ON DELETE");
                    }
                } else {
                    registrarError("Instrucción ON DELETE inválida");
                }
            } else if (tokenActual != null && tokenActual.getText().equals("UPDATE")) {
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals("CASCADE")) {
                    avanzar();
                } else {
                    registrarError("Se esperaba 'CASCADE' después de 'ON UPDATE'");
                }
            }
        }
        if (tokenActual != null && tokenActual.getText().equals(";")) {
            avanzar();
        } else {
            registrarError("Se esperaba ';' al final de la instrucción de restricción");
        }
    }




    private void analizarAlterColumn(String nombreTabla) {
    avanzar(); // Avanza después de ALTER
    if (tokenActual != null && tokenActual.getText().equals("COLUMN")) {
        avanzar();
        if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
            String nombreColumna = tokenActual.getText();
            avanzar();
            if (tokenActual != null && tokenActual.getText().equals("TYPE")) {
                avanzar();
                analizarTipoDeDatoAl(); // Aquí verificas el tipo de dato que se está definiendo
                if (tokenActual != null && tokenActual.getText().equals(";")) {
                    avanzar();
                } else {
                    registrarError("Se esperaba ';' al final de la instrucción ALTER COLUMN TYPE");
                }
            } else {
                registrarError("Se esperaba 'TYPE' después de 'ALTER COLUMN'");
            }
        } else {
            registrarError("Se esperaba un identificador para el nombre de la columna");
        }
    } else {
        registrarError("Se esperaba 'COLUMN' después de 'ALTER'");
    }
}


    // Método para analizar la eliminación de una columna
    private void analizarDropColumn(String nombreTabla) {
        avanzar(); // Avanza después de DROP
        if (tokenActual != null && tokenActual.getText().equals("COLUMN")) {
            avanzar();
            if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                String nombreColumna = tokenActual.getText();
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals(";")) {
                    avanzar();
                } else {
                    registrarError("Se esperaba ';' al final de la instrucción DROP COLUMN");
                }
            } else {
                registrarError("Se esperaba un identificador para el nombre de la columna");
            }
        } else {
            registrarError("Se esperaba 'COLUMN' después de 'DROP'");
        }
    }


    // Método para analizar DROP TABLE
    private void analizarDropTable() {
        avanzar(); // Avanza después de DROP
        if (tokenActual != null && tokenActual.getText().equals("TABLE")) {
            avanzar();
            if (tokenActual != null && tokenActual.getText().equals("IF")) {
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals("EXISTS")) {
                    avanzar();
                }
            }
            if (tokenActual != null && tokenActual.getType().equals("Identificador")) {
                String nombreTabla = tokenActual.getText();
                avanzar();
                if (tokenActual != null && tokenActual.getText().equals("CASCADE")) {
                    avanzar();
                }
                if (tokenActual != null && tokenActual.getText().equals(";")) {
                    avanzar();
                } else {
                    registrarError("Se esperaba ';' al final de la instrucción DROP TABLE");
                }
            } else {
                registrarError("Se esperaba un identificador para el nombre de la tabla");
            }
        } else {
            registrarError("Se esperaba 'TABLE' después de 'DROP'");
        }
    }

    // Método para analizar los tipos de datos permitidos
    private void analizarTipoDeDatoAl() {
        if (tokenActual != null) {
            switch (tokenActual.getText()) {
                case "SERIAL":
                case "INTEGER":
                case "BIGINT":
                case "DATE":
                case "TEXT":
                case "BOOLEAN":
                    avanzar();
                    break;
                case "VARCHAR":
                    avanzar();
                    if (tokenActual != null && tokenActual.getText().equals("(")) {
                        avanzar();
                        if (tokenActual != null && tokenActual.getType().equals("Entero")) {
                            avanzar();
                            if (tokenActual != null && tokenActual.getText().equals(")")) {
                                avanzar();
                            } else {
                                registrarError("Se esperaba ')' después del tamaño de VARCHAR");
                            }
                        } else {
                            registrarError("Se esperaba un entero para el tamaño de VARCHAR");
                        }
                    } else {
                        registrarError("Se esperaba '(' después de 'VARCHAR'");
                    }
                    break;
                case "DECIMAL":
                    avanzar();
                    if (tokenActual != null && tokenActual.getText().equals("(")) {
                        avanzar();
                        if (tokenActual != null && tokenActual.getType().equals("Entero")) {
                            avanzar();
                            if (tokenActual != null && tokenActual.getText().equals(",")) {
                                avanzar();
                                if (tokenActual != null && tokenActual.getType().equals("Entero")) {
                                    avanzar();
                                    if (tokenActual != null && tokenActual.getText().equals(")")) {
                                        avanzar();
                                    } else {
                                        registrarError("Se esperaba ')' después de la precisión y escala de DECIMAL");
                                    }
                                } else {
                                    registrarError("Se esperaba un entero para la escala de DECIMAL");
                                }
                            } else {
                                registrarError("Se esperaba ',' después de la precisión de DECIMAL");
                            }
                        } else {
                            registrarError("Se esperaba un entero para la precisión de DECIMAL");
                        }
                    } else {
                        registrarError("Se esperaba '(' después de 'DECIMAL'");
                    }
                    break;
                default:
                    registrarError("Tipo de dato no válido");
            }
        }
    }

}
