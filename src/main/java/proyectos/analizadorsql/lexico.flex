package proyectos.analizadorsql;

// Sección de imports
import java.util.ArrayList;
import java.util.List;

%%

/* Opciones de JFlex */
%class SQLLexer
%unicode
%function yylex
%type Token

%{
  /* Código Java */
  import java.util.regex.*;
  import com.analizador.sql.Token;

  /* Definir colores */
  public static final String COLOR_NARANJA = "#FFA500";
  public static final String COLOR_MORADO = "#800080";
  public static final String COLOR_AZUL = "#0000FF";
  public static final String COLOR_AMARILLO = "#FFFF00";
  public static final String COLOR_VERDE = "#008000";
  public static final String COLOR_FUCSIA = "#FF00FF";
  public static final String COLOR_GRIS = "#808080";
  public static final String COLOR_NEGRO = "#000000";

  private List<Token> tokens = new ArrayList<>();

  private void addToken(String text, String type, String color) {
      // Implementación para agregar el token al editor con el color correspondiente
      tokens.add(new Token(text, type, color));
      System.out.println("Token: " + text + " | Tipo: " + type + " | Color: " + color);
  }

  public List<Token> getTokens() {
      return tokens;
  }
%}

/* Expresiones regulares para los tokens */
CREATE          = CREATE|DATABASE|TABLE|KEY|NULL|PRIMARY|UNIQUE|FOREIGN|REFERENCES|ALTER|ADD|COLUMN|TYPE|DROP|CONSTRAINT|IF|EXIST|CASCADE|ON|DETELE|SET|UPDATE|INSERT|INTO|VALUES|SELECT|FROM|WHERE|AS|GROUP|ORDEN|BY|ASC|DESC|LIMIT|JOIN
TIPO_DE_DATO    = INTEGER|BIGINT|VARCHAR|DECIMAL|DATE|TEXT|BOOLEAN|SERIAL
BOOLEANO        = TRUE|FALSE
FUNCIONES_AGR   = SUM|AVG|COUNT|MAX|MIN
LOGICOS         = AND|OR|NOT
RELACIONALES    = <|>|<=|>=
ARITMATICOS     = \+|\-|\*|\/
SIGNOS          = [\(\),;.=]
COMMENTARIO     = "--"[^'\n']*

/* Definición de las reglas del analizador léxico */
%%

<YYINITIAL> {

  /* Palabras clave DDL */
  {CREATE}                { addToken(yytext(), "Palabra Clave DDL", COLOR_NARANJA); }

  /* Tipos de datos */
  {TIPO_DE_DATO }         { addToken(yytext(), "Tipo de Dato", COLOR_MORADO); }

  /* Números */
  [0-9]+               { addToken(yytext(), "Entero", COLOR_AZUL); }
  [0-9]+\.[0-9]+       { addToken(yytext(), "Decimal", COLOR_AZUL); }

  /* Fechas en formato 'YYYY-MM-DD' */
  \'[0-9]{4}-[0-9]{2}-[0-9]{2}\' { addToken(yytext(), "Fecha", COLOR_AMARILLO); }

  /* Cadenas */
  \'[^\']*\'            { addToken(yytext(), "Cadena", COLOR_VERDE); }

  /* Identificadores en snake_case */
  [a-z]+(_[a-z0-9]+)*   { addToken(yytext(), "Identificador", COLOR_FUCSIA); }

  /* Booleanos */
  {BOOLEANO}             { addToken(yytext(), "Booleano", COLOR_AZUL); }

  /* Funciones de agregación */
  {FUNCIONES_AGR}       { addToken(yytext(), "Función de Agregación", COLOR_AZUL); }

  /* Operadores relacionales */
  {RELACIONALES}       { addToken(yytext(), "Operador Relacional", COLOR_NEGRO); }

  /* Operadores aritméticos */
  {ARITMATICOS}       { addToken(yytext(), "Operador Aritmético", COLOR_NEGRO); }

  /* Signos */
  {SIGNOS}               { addToken(yytext(), "Signo", COLOR_NEGRO); }

  /* Operadores lógicos */
  {LOGICOS}             { addToken(yytext(), "Operador Lógico", COLOR_NARANJA); }

  /* Comentarios */
  {COMMENTARIO}             { addToken(yytext(), "Comentario", COLOR_GRIS); }

  /* Ignorar espacios en blanco */
  [ \t\r\n]+            { /* Ignorar */ }

  /* Otros caracteres no reconocidos */
  .                     { System.err.println("Símbolo no reconocido: " + yytext()); }
}
