/* Generated By:JavaCC: Do not edit this line. Qss.java */
import java.io.*;
import java.util.*;

public class Qss implements QssConstants {
    private static final String DEFAULT_EOL = "\n\r";

    private Stack stack;
    private Evidence evidence;
    private Writer debugWriter = null;
    private Writer errorWriter = null;
    private String debugEol = DEFAULT_EOL;
    private String errorEol = DEFAULT_EOL;
    private int errorCount = 0;
    private boolean isDebug = false;

        public Datum parse(Evidence ev) {
                evidence = ev;
        Datum d = Datum.INVALID_DATUM;

                try {
                        stack = new Stack();
                        Statement();
                        d = (Datum) stack.pop();
                }
                catch (EmptyStackException e) {
                        error("stack underflow",token.beginLine,token.beginColumn);
                }
                catch (ParseException e) {
                        error(e.getMessage());
                }
                catch (TokenMgrError e) {
                        error(e.getMessage());
                }

                if (isDebug) {
                        debug(null,d);
                }

                return d;
        }

        public boolean setDebugWriter(Writer w) { return setDebugWriter(w,null); }
        public boolean setDebugWriter(Writer w, String eol) {
                debugWriter = w;
                debugEol = ((eol == null) ? DEFAULT_EOL : eol);
                isDebug = (w != null);
                return isDebug;
        }

        public boolean setErrorWriter(Writer w) { return setErrorWriter(w,null); }
        public boolean setErrorWriter(Writer w, String eol) {
                errorWriter = w;
                errorEol = ((eol == null) ? DEFAULT_EOL : eol);
                return (w != null);
        }


        private void error(String s) { error(s,0,0); }

        private void error(String s, int line, int column) {
                if (errorWriter != null) {
                        try {
                                errorWriter.write("ERR " + (++errorCount) + ") " + s +
                                        ((line != 0) ? (" at line " + line + ", column " + column) : "") + errorEol);
                                errorWriter.flush();
                        }
                        catch (IOException e) {
                                System.err.println(e.getMessage());
                        }
                }
        }

        /* Prints stack trace in tab delimited format - operator, arguments, ->, answer */
        private void debug(String s,Datum d) {
                if (debugWriter != null) {
                        try {
                                debugWriter.write(((s != null) ? (s + "\t") : "") + "->\t" +
                                        d.stringVal(true) + "\t" + d.doubleVal() + "\t" + d.dateVal() + "\t" + d.monthVal() + debugEol);
                                debugWriter.flush();
                        }
                        catch (IOException e) {
                                System.err.println(e.getMessage());
                        }
                }
        }

        private String datumValue(Datum d) {
                return ("(" + d.getName() + "," + d.stringVal(true) + ")");
        }

        private String opName(int op) {
                return tokenImage[op].substring(1,tokenImage[op].length()-1);
        }

        private void unaryOp(int op, Object arg1) {
                Datum a = getParam(arg1);
                Datum ans = Datum.INVALID_DATUM;
                switch(op) {
                        case PLUS: ans = a; break;
                        case MINUS: ans = DatumMath.neg(a); break;
                        case NOT: ans = DatumMath.not(a); break;
                }
                stack.push(ans);
                if (isDebug) {
                        debug(opName(op) + "\t" + datumValue(a),ans);
                }
        }

        private Datum getParam(Object o) {
                if (o == null)
                        return Datum.INVALID_DATUM;
                return (Datum) o;
        }

        private void binaryOp(int op, Object arg2, Object arg1) {
                Datum a = getParam(arg1);
                Datum b = getParam(arg2);
                Datum ans = Datum.INVALID_DATUM;
                switch(op) {
                        case PLUS: ans = DatumMath.add(a,b); break;
                        case MINUS: ans = DatumMath.subtract(a,b); break;
                        case MULTIPLY: ans = DatumMath.multiply(a,b); break;
                        case DIVIDE: ans = DatumMath.divide(a,b); break;
                        case GT: ans = DatumMath.gt(a,b); break;
                        case GE: ans = DatumMath.ge(a,b); break;
                        case EQ: ans = DatumMath.eq(a,b); break;
                        case NEQ: ans = DatumMath.neq(a,b); break;
                        case LT: ans = DatumMath.lt(a,b); break;
                        case LE: ans = DatumMath.le(a,b); break;
                        case CONCATENATE: ans = DatumMath.concat(a,b); break;
                        case ANDAND: ans = DatumMath.andand(a,b); break;
                        case OROR: ans = DatumMath.oror(a,b); break;
                        case MODULUS: ans = DatumMath.modulus(a,b); break;
                        case XOR: ans = DatumMath.xor(a,b); break;
                        case AND: ans = DatumMath.and(a,b); break;
                        case OR: ans = DatumMath.or(a,b); break;
                        case ASSIGN: evidence.set(a.stringVal(),b); ans = evidence.getDatum(a.stringVal()); break;
                }
                stack.push(ans);
                if (isDebug) {
                        debug(opName(op) + "\t" + datumValue(a) + "\t" + datumValue(b),ans);
                }
        }

        private void trinaryOp(int op, Object arg3, Object arg2, Object arg1) {
                Datum a = getParam(arg1);
                Datum b = getParam(arg2);
                Datum c = getParam(arg3);
                Datum ans = Datum.INVALID_DATUM;
                switch(op) {
                        case QUEST: ans = DatumMath.conditional(a,b,c); break;
                }
                stack.push(ans);
                if (isDebug) {
                        debug(opName(op) + "\t" + datumValue(a) + "\t" + datumValue(b) + "\t" + datumValue(c),ans);
                }
        }

        private void functionOp(Token func, Stack params) {
                Datum ans = Datum.INVALID_DATUM;
                ans = evidence.function(func.image, params, func.beginLine, func.beginColumn);
                stack.push(ans);
                if (isDebug) {
                        StringBuffer sb = new StringBuffer("function\t" + func.image);
                        for (int i=0;i<params.size();++i) {
                                Object o = params.elementAt(i);
                                if (o == null)
                                        sb.append("\tnull");
                                else if (o instanceof Datum)
                                        sb.append("\t" + datumValue((Datum) o));
                                else if (o instanceof String)
                                        sb.append("\t" + (String) o);
                                else
                                        sb.append("\t" + o.getClass());
                        }
                        debug(sb.toString(), ans);
                }
        }

  final public void Statement() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CHARACTER_LITERAL:
    case STRING_LITERAL:
    case PLUS:
    case MINUS:
    case NOT:
    case LP:
    case LSB:
    case LCB:
    case INTEGER_LITERAL:
    case FLOATING_POINT_LITERAL:
    case NMTOKEN:
      Expressions();
      break;
    case SEMICOLON:
      jj_consume_token(SEMICOLON);
      break;
    case EOL:
      jj_consume_token(EOL);
      break;
    case 0:
      jj_consume_token(0);
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void Expressions() throws ParseException {
    Expression();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      jj_consume_token(COMMA);
      Expression();
                                        Object value = stack.pop();
                                        stack.pop();    /* throw away the penultimate item on the stack */
                                        stack.push(value);
    }
  }

  final public void Expression() throws ParseException {
    if (jj_2_1(2)) {
      AssignmentExpression();
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CHARACTER_LITERAL:
      case STRING_LITERAL:
      case PLUS:
      case MINUS:
      case NOT:
      case LP:
      case LSB:
      case LCB:
      case INTEGER_LITERAL:
      case FLOATING_POINT_LITERAL:
      case NMTOKEN:
        ConditionalExpression();
        break;
      default:
        jj_la1[2] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
  }

  final public void AssignmentExpression() throws ParseException {
                               Token t;
    jj_consume_token(NMTOKEN);
                    t = token;
    jj_consume_token(ASSIGN);
    ConditionalExpression();
                binaryOp(ASSIGN,stack.pop(), new Datum(t.image,Datum.STRING));
  }

  final public void ConditionalExpression() throws ParseException {
    LogicalORExpression();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case QUEST:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_2;
      }
      jj_consume_token(QUEST);
      LogicalORExpression();
      jj_consume_token(COLON);
      LogicalORExpression();
                                  trinaryOp(QUEST,stack.pop(),stack.pop(),stack.pop());
    }
  }

  final public void LogicalORExpression() throws ParseException {
    LogicalANDExpression();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OROR:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_3;
      }
      jj_consume_token(OROR);
      LogicalANDExpression();
                                  binaryOp(OROR,stack.pop(),stack.pop());
    }
  }

  final public void LogicalANDExpression() throws ParseException {
    InclusiveORExpression();
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ANDAND:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_4;
      }
      jj_consume_token(ANDAND);
      InclusiveORExpression();
                                  binaryOp(ANDAND,stack.pop(),stack.pop());
    }
  }

  final public void InclusiveORExpression() throws ParseException {
    ExclusiveORExpression();
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_5;
      }
      jj_consume_token(OR);
      ExclusiveORExpression();
                                  binaryOp(OR,stack.pop(),stack.pop());
    }
  }

  final public void ExclusiveORExpression() throws ParseException {
    ANDExpression();
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case XOR:
        ;
        break;
      default:
        jj_la1[7] = jj_gen;
        break label_6;
      }
      jj_consume_token(XOR);
      ANDExpression();
                                  binaryOp(XOR,stack.pop(),stack.pop());
    }
  }

  final public void ANDExpression() throws ParseException {
    EqualityExpression();
    label_7:
    while (true) {
      if (jj_2_2(2)) {
        ;
      } else {
        break label_7;
      }
      jj_consume_token(AND);
      EqualityExpression();
                                  binaryOp(AND,stack.pop(),stack.pop());
    }
  }

  final public void EqualityExpression() throws ParseException {
                              Token op;
    RelationalExpression();
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case EQ:
      case NEQ:
        ;
        break;
      default:
        jj_la1[8] = jj_gen;
        break label_8;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case EQ:
        jj_consume_token(EQ);
        break;
      case NEQ:
        jj_consume_token(NEQ);
        break;
      default:
        jj_la1[9] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                           op = token;
      RelationalExpression();
                                  binaryOp(op.kind,stack.pop(),stack.pop());
    }
  }

  final public void RelationalExpression() throws ParseException {
                                Token op;
    AdditiveExpression();
    label_9:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case GT:
      case GE:
      case LT:
      case LE:
        ;
        break;
      default:
        jj_la1[10] = jj_gen;
        break label_9;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LT:
        jj_consume_token(LT);
        break;
      case GT:
        jj_consume_token(GT);
        break;
      case LE:
        jj_consume_token(LE);
        break;
      case GE:
        jj_consume_token(GE);
        break;
      default:
        jj_la1[11] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                                        op = token;
      AdditiveExpression();
                                  binaryOp(op.kind,stack.pop(),stack.pop());
    }
  }

  final public void AdditiveExpression() throws ParseException {
                              Token op;
    MultiplicativeExpression();
    label_10:
    while (true) {
      if (jj_2_3(2)) {
        ;
      } else {
        break label_10;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        jj_consume_token(PLUS);
        break;
      case MINUS:
        jj_consume_token(MINUS);
        break;
      case CONCATENATE:
        jj_consume_token(CONCATENATE);
        break;
      default:
        jj_la1[12] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                                               op = token;
      MultiplicativeExpression();
                                  binaryOp(op.kind,stack.pop(),stack.pop());
    }
  }

  final public void MultiplicativeExpression() throws ParseException {
                                    Token op;
    UnaryExpression();
    label_11:
    while (true) {
      if (jj_2_4(2)) {
        ;
      } else {
        break label_11;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case MULTIPLY:
        jj_consume_token(MULTIPLY);
        break;
      case DIVIDE:
        jj_consume_token(DIVIDE);
        break;
      default:
        jj_la1[13] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                                    op = token;
      UnaryExpression();
                                  binaryOp(op.kind,stack.pop(),stack.pop());
    }
  }

  final public void UnaryExpression() throws ParseException {
                           Token op;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PLUS:
    case MINUS:
    case NOT:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        jj_consume_token(PLUS);
        break;
      case MINUS:
        jj_consume_token(MINUS);
        break;
      case NOT:
        jj_consume_token(NOT);
        break;
      default:
        jj_la1[14] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                     op = token;
      PrimaryExpression();
                  unaryOp(op.kind,stack.pop());
      break;
    case CHARACTER_LITERAL:
    case STRING_LITERAL:
    case LP:
    case LSB:
    case LCB:
    case INTEGER_LITERAL:
    case FLOATING_POINT_LITERAL:
    case NMTOKEN:
      PrimaryExpression();
      break;
    default:
      jj_la1[15] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void PrimaryExpression() throws ParseException {
    if (jj_2_5(2)) {
      Function();
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CHARACTER_LITERAL:
      case STRING_LITERAL:
      case INTEGER_LITERAL:
      case FLOATING_POINT_LITERAL:
        Constant();
        break;
      case NMTOKEN:
        jj_consume_token(NMTOKEN);
                        Datum d = evidence.getDatum(token.image);
                        if (d == null) {
                                error("undefined variable '" + token.image + "'", token.beginLine, token.beginColumn);
                                stack.push(Datum.INVALID_DATUM);
                        }
                        else {
                                stack.push(d);
                        }
        break;
      case LP:
        jj_consume_token(LP);
        Expressions();
        jj_consume_token(RP);
        break;
      case LCB:
        jj_consume_token(LCB);
        Expressions();
        jj_consume_token(RCB);
        break;
      case LSB:
        jj_consume_token(LSB);
        Expressions();
        jj_consume_token(RSB);
        break;
      default:
        jj_la1[16] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
  }

  final public void Constant() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INTEGER_LITERAL:
    case FLOATING_POINT_LITERAL:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INTEGER_LITERAL:
        jj_consume_token(INTEGER_LITERAL);
        break;
      case FLOATING_POINT_LITERAL:
        jj_consume_token(FLOATING_POINT_LITERAL);
        break;
      default:
        jj_la1[17] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                  stack.push(new Datum(token.image,Datum.NUMBER));
      break;
    case CHARACTER_LITERAL:
    case STRING_LITERAL:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STRING_LITERAL:
        jj_consume_token(STRING_LITERAL);
        break;
      case CHARACTER_LITERAL:
        jj_consume_token(CHARACTER_LITERAL);
        break;
      default:
        jj_la1[18] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                        /* replace special characters before storing value */
                        StringBuffer sb = new StringBuffer();
                        char c;
                        int i=0;

                        try {
                                for (i=1;i<token.image.length()-1;++i) {
                                        c = token.image.charAt(i);
                                        if (c == '\\') {
                                                c = token.image.charAt(++i);
                                                switch (c) {
                                                        case 'b': sb.append('\b'); break;
                                                        case 'f': sb.append('\f'); break;
                                                        case 'n': sb.append('\n'); break;
                                                        case 'r': sb.append('\n'); break;
                                                        case 't': sb.append('\t'); break;
                                                        case '\'': sb.append('\''); break;
                                                        case '\"': sb.append('\"'); break;
                                                        case '\\': sb.append('\\'); break;
                                                        default: sb.append(c); break;
                                                }
                                        }
                                        else {
                                                sb.append(c);
                                        }
                                }
                        }
                        catch (IndexOutOfBoundsException e) {
                                error("unterminated escaped character", token.beginLine, token.beginColumn + i);
                        }

                        stack.push(new Datum(sb.toString(),Datum.STRING));
      break;
    default:
      jj_la1[19] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void Function() throws ParseException {
                    Token t; Stack params;
    jj_consume_token(NMTOKEN);
                    t = token;
    jj_consume_token(LP);
    params = FunctionParameters();
    jj_consume_token(RP);
                        functionOp(t,params);
  }

  final public Stack FunctionParameters() throws ParseException {
                               Stack params = new Stack();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CHARACTER_LITERAL:
    case STRING_LITERAL:
    case PLUS:
    case MINUS:
    case NOT:
    case LP:
    case LSB:
    case LCB:
    case INTEGER_LITERAL:
    case FLOATING_POINT_LITERAL:
    case NMTOKEN:
      Expression();
                  params.push(stack.pop());
      label_12:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case COMMA:
          ;
          break;
        default:
          jj_la1[20] = jj_gen;
          break label_12;
        }
        jj_consume_token(COMMA);
        Expression();
                          params.push(stack.pop());
      }
                  {if (true) return params;}
      break;
    default:
      jj_la1[21] = jj_gen;
          {if (true) return params;}
    }
    throw new Error("Missing return statement in function");
  }

  final private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_1();
    jj_save(0, xla);
    return retval;
  }

  final private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_2();
    jj_save(1, xla);
    return retval;
  }

  final private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_3();
    jj_save(2, xla);
    return retval;
  }

  final private boolean jj_2_4(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_4();
    jj_save(3, xla);
    return retval;
  }

  final private boolean jj_2_5(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_5();
    jj_save(4, xla);
    return retval;
  }

  final private boolean jj_3R_23() {
    if (jj_3R_26()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_22() {
    if (jj_scan_token(NMTOKEN)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(LP)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3_2() {
    if (jj_scan_token(AND)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_14()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_14() {
    if (jj_3R_23()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_42() {
    if (jj_scan_token(CHARACTER_LITERAL)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_40() {
    if (jj_scan_token(FLOATING_POINT_LITERAL)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_41() {
    if (jj_scan_token(STRING_LITERAL)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_39() {
    if (jj_scan_token(INTEGER_LITERAL)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_38() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_41()) {
    jj_scanpos = xsp;
    if (jj_3R_42()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_37() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_39()) {
    jj_scanpos = xsp;
    if (jj_3R_40()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_36() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_37()) {
    jj_scanpos = xsp;
    if (jj_3R_38()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_20() {
    if (jj_scan_token(DIVIDE)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_35() {
    if (jj_scan_token(LSB)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_34() {
    if (jj_scan_token(LCB)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_17() {
    if (jj_scan_token(CONCATENATE)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_33() {
    if (jj_scan_token(LP)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_29() {
    if (jj_scan_token(NOT)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_19() {
    if (jj_scan_token(MULTIPLY)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_16() {
    if (jj_scan_token(MINUS)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_28() {
    if (jj_scan_token(MINUS)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_32() {
    if (jj_scan_token(NMTOKEN)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_31() {
    if (jj_3R_36()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_30() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_5()) {
    jj_scanpos = xsp;
    if (jj_3R_31()) {
    jj_scanpos = xsp;
    if (jj_3R_32()) {
    jj_scanpos = xsp;
    if (jj_3R_33()) {
    jj_scanpos = xsp;
    if (jj_3R_34()) {
    jj_scanpos = xsp;
    if (jj_3R_35()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3_5() {
    if (jj_3R_22()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_13() {
    if (jj_scan_token(NMTOKEN)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(ASSIGN)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_25() {
    if (jj_3R_30()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_15() {
    if (jj_scan_token(PLUS)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3_4() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_19()) {
    jj_scanpos = xsp;
    if (jj_3R_20()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_21()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_27() {
    if (jj_scan_token(PLUS)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_24() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_27()) {
    jj_scanpos = xsp;
    if (jj_3R_28()) {
    jj_scanpos = xsp;
    if (jj_3R_29()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_21() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_24()) {
    jj_scanpos = xsp;
    if (jj_3R_25()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3_1() {
    if (jj_3R_13()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3_3() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_15()) {
    jj_scanpos = xsp;
    if (jj_3R_16()) {
    jj_scanpos = xsp;
    if (jj_3R_17()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_18()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_18() {
    if (jj_3R_21()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_26() {
    if (jj_3R_18()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  public QssTokenManager token_source;
  ASCII_CharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  public boolean lookingAhead = false;
  private boolean jj_semLA;
  private int jj_gen;
  final private int[] jj_la1 = new int[22];
  final private int[] jj_la1_0 = {0xaa00003f,0x1000000,0x2a00003e,0x400000,0x10000,0x8000,0x100000,0x40000,0xc00,0xc00,0x3300,0x3300,0x4018,0xc0,0x38,0x2a00003e,0x2a000006,0x0,0x6,0x6,0x1000000,0x2a00003e,};
  final private int[] jj_la1_1 = {0x688,0x0,0x288,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x288,0x288,0x88,0x0,0x88,0x0,0x288,};
  final private JJCalls[] jj_2_rtns = new JJCalls[5];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  public Qss(java.io.InputStream stream) {
    jj_input_stream = new ASCII_CharStream(stream, 1, 1);
    token_source = new QssTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public Qss(java.io.Reader stream) {
    jj_input_stream = new ASCII_CharStream(stream, 1, 1);
    token_source = new QssTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public Qss(QssTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(QssTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    return (jj_scanpos.kind != kind);
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (java.util.Enumeration enum = jj_expentries.elements(); enum.hasMoreElements();) {
        int[] oldentry = (int[])(enum.nextElement());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.addElement(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  final public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[43];
    for (int i = 0; i < 43; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 22; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 43; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

  final private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 5; i++) {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
          }
        }
        p = p.next;
      } while (p != null);
    }
    jj_rescan = false;
  }

  final private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
