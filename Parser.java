import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Statement;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import java.util.Set;
import java.util.HashSet;

class Parser {

    private static ArrayList<TypeInfo> variableMap = 
        new ArrayList<TypeInfo>();
 
    private static int currentLevel = 0;

    static class TypeInfo {
        public String varName;
        public int level;
        public String varType;

        TypeInfo(String varName, int level, String varType) { 
            this.varName = varName; 
            this.level = level; 
            this.varType = varType;
        }

        public static String getMappedType(String variableName, ArrayList<TypeInfo> mapList) {
            
            //for (TypeInfo map : mapList) {
            //    System.out.println(map.varName +  " " + map.varType);
           // }

            for (int i = mapList.size() - 1; i >= 0; i--) {
                TypeInfo map = mapList.get(i);
                if (map.varName.equals(variableName)) {
                    return map.varType;
                }
            }
            return null;
        }
    }


    /* Convert a file into a String */
    private static String fileToString(String path) throws IOException {
               
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    
    private static void clearLocalVariables() {
        for (int i = variableMap.size()-1; i >= 0; i--) {

            if (variableMap.get(i).level == currentLevel) {
                //variableMap.remove(variableMap.get(i));
                variableMap.remove(i);
            }
        }
    }

	/* Convert files that are in String format to AST */
    public static Tokenizer parseAST2Tokens(String absPath, int minNumLines, boolean debug) {
        String source = "";
        try {
            source = fileToString(absPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ASTParser parser = ASTParser.newParser(AST.JLS4);

        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        char[] content = source.toCharArray();
        parser.setSource(content);
        parser.setUnitName(absPath);
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
            JavaCore.VERSION_1_7);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
        
        // new String[] {"UTF-8"}
            String[] sources = {};
          String[] classPaths = {};
        parser.setEnvironment(classPaths, sources,
            null, true);
        //parser.setBindingsRecovery(true);
        parser.setResolveBindings(false);
        parser.setCompilerOptions(options);
        parser.setStatementsRecovery(true);

        //System.out.println(absPath);

        try {
            final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
            final AST ast = unit.getAST();

            // Process the main body
            final Tokenizer tk = new Tokenizer(minNumLines, debug);
            try {
                unit.accept(new ASTVisitor() {
                
                    /*
                    // JavaDoc comment that starts with "/** for type declarations"
                    public boolean	visit(AnnotationTypeDeclaration node) {
                        
                        int line_number = unit.getLineNumber(node.getStartPosition());
                        return true;
                    }
                    
                    // JavaDoc comment that starts with "/** for type member declarations"
                    public boolean	visit(AnnotationTypeMemberDeclaration node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        return true;
                    }
                    */

                    public boolean	visit(AnonymousClassDeclaration node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.AnonymousClassDeclaration.ordinal(), line_number);

                        return true;
                    }
                    
                    public boolean	visit(ArrayAccess node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.ArrayAccess.ordinal(), line_number);

                        return true;
                    }

                    public boolean	visit(ArrayCreation node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.ArrayCreation.ordinal(), line_number);
                        return true;
                    }

                    public boolean	visit(ArrayInitializer node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.ArrayInitializer.ordinal(), line_number);

                        return true;
                    }

                    public boolean	visit(ArrayType node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        ITypeBinding binding = node.getElementType().resolveBinding();
                        if (binding != null) {
                            tk.addHash(TokenType.ArrayType.ordinal(),binding.getQualifiedName(), line_number);
                        }
                        else {
                            tk.addHash(TokenType.ArrayType.ordinal(), line_number);
                        }

                        return true;
                    }

                    public boolean	visit(AssertStatement node) {
                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.AssertStatement.ordinal(), line_number);

                        return true;
                    }

                    public void endVisit(AssertStatement node) {
                        tk.statementEnd(currentLevel);
                    }

                    public boolean	visit(Assignment node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());				
                        String operator = node.getOperator().toString();
                        tk.addHash(TokenType.Assignment.ordinal(), operator, line_number);

                        return true;
                    }
                    
                    public boolean	visit(Block node) {
                        currentLevel = currentLevel + 1;

                        tk.statementEnd(currentLevel);

                        return true;
                    }

                    public void endVisit(Block node) {
                        clearLocalVariables();

                        currentLevel = currentLevel - 1;
                    }

                    public boolean	visit(BooleanLiteral node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        if (node.booleanValue()) {
                            tk.addHash(TokenType.BooleanLiteral.ordinal(), "true", line_number);
                        } else {
                            tk.addHash(TokenType.BooleanLiteral.ordinal(), "false", line_number);
                        }
                        return true;
                    }

                    public boolean	visit(BreakStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.BreakStatement.ordinal(), line_number);

                        return true;
                    }

                    public void endVisit(BreakStatement node) {
                        tk.statementEnd(currentLevel);
                    }

                    public boolean	visit(CastExpression node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.CastExpression.ordinal(), line_number); //reminder
                        return true;
                    }

                    public boolean	visit(CatchClause node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.CatchClause.ordinal(), line_number);

                        return true;
                    }

                    public boolean	visit(CharacterLiteral node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.CharacterLiteral.ordinal(), node.getEscapedValue(), line_number);
                        return true;
                    }

                    public boolean	visit(ClassInstanceCreation node) {

                        currentLevel = currentLevel + 1;

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.ClassInstanceCreation.ordinal(), line_number);
                        return true;
                    }

                    public void endVisit(ClassInstanceCreation node) {
                        currentLevel = currentLevel - 1;
                    }

                    /*
                    public boolean	visit(CompilationUnit node) {
                        int line_number = unit.getLineNumber(node.getStartPosition());
                        return true;
                    }
                    */

                    public boolean	visit(ConditionalExpression node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.ConditionalExpression.ordinal(), line_number);

                        return true;
                    }

                    public boolean	visit(ConstructorInvocation node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.SuperConstructorInvocation.ordinal(), line_number);

                        return true;
                    }

                    public void endVisit(ConstructorInvocation node) {
                        tk.statementEnd(currentLevel);
                    }

                    public boolean	visit(ContinueStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.ContinueStatement.ordinal(), line_number);

                        return true;
                    }

                    public void endVisit(ContinueStatement node) {
                        tk.statementEnd(currentLevel);
                    }

                    public boolean	visit(DoStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());	
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.DoStatement.ordinal(), line_number);

                        return true;
                    }

                    public void endVisit(DoStatement node) {
                        tk.statementEnd(currentLevel);
                    }

                    /*
                    public boolean	visit(EmptyStatement node) {
                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);

                        tk.addHash(78, line_number);
                        tk.statementEnd(currentLevel);

                        return true;
                    }*/

                    public boolean	visit(EnhancedForStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.EnhancedForStatement.ordinal(), line_number);

                        return true;
                    }

                    /*
                    public boolean	visit(EnumConstantDeclaration node) {
                        System.out.println("EnumConstantDeclaration");

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        System.out.println("EnumConstantDeclaration at line "+ Integer.toString(line_number));
                        return true;
                    }*/

                    /*
                    public boolean	visit(EnumDeclaration node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        return true;
                    }
                    */

                    public boolean	visit(ExpressionStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);

                        Expression exp = node.getExpression();

                        return true;
                    }

                    public void endVisit(ExpressionStatement node) {
                        tk.statementEnd(currentLevel);
                    }

                    public boolean	visit(FieldAccess node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        String fieldName = node.getName().toString();
                        tk.addHash(TokenType.FieldAccess.ordinal(), fieldName, line_number);

                        return true;
                    }
                    /*
                     * always together with VariableDeclarationFragment
                     */
                    public boolean	visit(FieldDeclaration node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        Type type = node.getType();
                        ITypeBinding binding = type.resolveBinding();

                        List<VariableDeclarationFragment> fragments = node.fragments();
                        for (VariableDeclarationFragment fragment : fragments) {
                            String name = fragment.getName().toString();
                            if (binding != null) {

                                TypeInfo typeInfo = new TypeInfo(
                                        name, currentLevel, binding.getName());

                                variableMap.add(typeInfo);
                                /*
                                if (!within_method.isEmpty()) {
                                    // In a method
                                    tk.statementStart(line_number);
                                    tk.addHash(85, binding.getName(), line_number);
                                }
                                */
                            } else {

                                String varType = node.getType().toString();
                                TypeInfo typeInfo = new TypeInfo(
                                        name, currentLevel, varType);

                                variableMap.add(typeInfo);
                                /*
                                if (!within_method.isEmpty()) {
                                    // In a method
                                    tk.addHash(85, varType, line_number);
                                }
                                */
                            }
                        }
                        return true;
                    }

                    public boolean	visit(ForStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);

                        tk.addHash(TokenType.ForStatement.ordinal(),line_number);

                        return true;
                    }

                    public boolean	visit(IfStatement node) {
                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        ASTNode parent = node.getParent();
                        boolean if_else_happen = false;
                        if (parent != null) {
                            if (parent.getNodeType() == 25) {
                                Statement else_statement = ((IfStatement) parent).getElseStatement();
                                if (else_statement != null) {
                                    if (else_statement.equals(node)) {
                                        if_else_happen = true;
                                    }
                                }
                            }
                        }

                        if (if_else_happen) {
                            tk.addHash(TokenType.Elseif.ordinal(), line_number);
                        } else {
                            tk.addHash(TokenType.IfStatement.ordinal(),line_number);
                        }

                        return true;
                    }
                    
                    public void	endVisit(IfStatement node) {
                        int line_number = unit.getLineNumber(node.getStartPosition()+node.getLength());
                        ASTNode parent = node.getParent();
                        if (parent != null) {
                            if (parent.getNodeType() == 25) {
                                Statement else_statement = ((IfStatement) parent).getElseStatement();
                                if (else_statement != null) {
                                    if (else_statement.equals(node)) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    
                    public void	preVisit(ASTNode node) {
                        ASTNode parent = node.getParent();
                        int line_number = unit.getLineNumber(node.getStartPosition());
                        
                        if (parent != null) {
                            if (parent.getNodeType() == 25 && node.getNodeType() != 25) {
                                Statement else_statement = ((IfStatement) parent).getElseStatement();
                                if (else_statement != null) {
                                    if (else_statement.equals(node)) {
                                        tk.statementStart(line_number);
                                        tk.addHash(TokenType.Else.ordinal(), line_number);
                                        tk.statementEnd(currentLevel);
                                    }
                                }
                            }
                        }
                    }
                    
                    public boolean	visit(InfixExpression node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        String operator = node.getOperator().toString();
                        tk.addHash(TokenType.InfixExpression.ordinal(), operator,line_number);

                        return true;
                    }

                    /*
                    public boolean	visit(Initializer node) {
                        System.out.println("Initializer");

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(28,line_number);

                        return true;
                    }*/

                    public boolean	visit(InstanceofExpression node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.InstanceofExpression.ordinal(),line_number);
                        return true;
                    }

                    /* Comments */
        //			public boolean	visit(Javadoc node) {
        //
        //				int line_number = unit.getLineNumber(node.getStartPosition());
        //
        //				System.out.println("Javadoc at line "+ Integer.toString(line_number));
        //
        //				return true;
        //			}

                    public boolean	visit(LabeledStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.LabeledStatement.ordinal(), line_number);
                        tk.statementEnd(currentLevel);

                        return true;
                    }
                    
                    /*
                    public boolean visit(Annotation node) {

                        System.out.println("annotation node");

                        return true;

                    }

                    public boolean	visit(MarkerAnnotation node) {

                        return false;
                        //int line_number = unit.getLineNumber(node.getStartPosition());
                        //tk.addHash(31, node.getTypeName().getFullyQualifiedName(),line_number);

                        //return true;
                    }
                    */

                    public boolean	visit(MemberRef node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.MemberRef.ordinal(), node.resolveBinding().getName(), line_number);

                        return true;
                    }

                    public boolean	visit(MemberValuePair node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.MemberValuePair.ordinal(), line_number);

                        return true;
                    }

                    public boolean	visit(MethodDeclaration node) {
                        tk.inMethod = true;

                        // Get Line number
                        int line_number = unit.getLineNumber(node.getStartPosition());
                        
                        tk.methodStart(node.getName().toString(), line_number);

                        tk.statementStart(line_number);

                        tk.addHash(TokenType.MethodDeclaration.ordinal(), line_number);

                        return true;
                    }
                    
                    public void endVisit(MethodDeclaration node) {
                        tk.inMethod = false;

                        int line_number = unit.getLineNumber(node.getStartPosition()+node.getLength());
                        //tk.statementStart(line_number);
                        //tk.addHash(75, line_number);
                        //tk.statementEnd(currentLevel);

                        //IMethodBinding methodBinding = node.resolveBinding();
                        tk.methodEnd(node.getName().toString(), line_number);
                        //System.out.println(node.getName().toString());
                    }

                    public boolean	visit(MethodInvocation node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.MethodInvocation.ordinal(), line_number);
                        tk.hasMethodInvocation();

                        return true;
                    }

                    /*
                    public boolean	visit(MethodRef node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        System.out.println("MethodRef at line "+ Integer.toString(line_number));

                        return true;
                    }

                    public boolean	visit(MethodRefParameter node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        System.out.println("MethodRefParameter at line "+ Integer.toString(line_number));

                        return true;
                    }*/


                    /*	public, protected, private, static, abstract, final, native, synchronized, transient
                     *  volatile, strictfp
                     */
                    /*
                    public boolean	visit(Modifier node) {
                        System.out.println("modifier node");
                        return true;
                    }
                    */
                 

                    /*
                    public boolean	visit(NormalAnnotation node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        System.out.println("Initializer at line "+ Integer.toString(line_number));
                        return true;
                    }
                    */

                    public boolean	visit(NullLiteral node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.NullLiteral.ordinal(), line_number);
                        return true;
                    }

                    public boolean	visit(NumberLiteral node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.NumberLiteral.ordinal(), line_number);

                        //System.out.println("NumberLiteral at line "+ Integer.toString(line_number));

                        return true;
                    }

                    public boolean visit(ImportDeclaration node) {
                        return false;
                    }

                    public boolean	visit(PackageDeclaration node) {
                        return false;
        //
        //				//int line_number = unit.getLineNumber(node.getStartPosition());
        //
        ////				SimpleName name = (SimpleName) node.getName();
        ////				System.out.println("PackageDeclaration with name of "+name+" at line "+ unit.getLineNumber(name.getStartPosition()));
        //				System.out.println("PackageDeclaration with name of ");
        //
        //				return true;
                    }

                    /*
                    public boolean	visit(ParameterizedType node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(43, line_number);

                        return true;
                    }*/

                    public boolean	visit(ParenthesizedExpression node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());				
                        tk.addHash(TokenType.ParenthesizedExpression.ordinal(), line_number);
                        return true;
                    } 

                    public boolean	visit(PostfixExpression node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        Expression operand = node.getOperand();
                        tk.addHash(TokenType.PostfixExpression.ordinal(),node.getOperator().toString(), line_number);


                        return true;
                    }

                    public boolean	visit(PrefixExpression node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        String operator = node.getOperator().toString();
                        tk.addHash(TokenType.InfixOperator.ordinal(), operator, line_number);
                        ITypeBinding binding = node.getOperand().resolveTypeBinding();

                        if (binding != null) {
                            tk.addHash(TokenType.PrefixExpression.ordinal(), binding.getQualifiedName(), line_number);
                        }
                        else {
                            tk.addHash(TokenType.PrefixExpression.ordinal(), line_number);
                        }
                        return true;
                    }

                    /*
                    public boolean visit(Name node) {

                        System.out.println("ddd");
                        return true;
                    }*/

                    /*
                     * always together with VariableDeclarationFragment
                     */
        //			public boolean	visit(PrimitiveType node) {
        //
        //				int line_number = unit.getLineNumber(node.getStartPosition());
        //				tk.addHash(47, node.toString(), line_number);
        //				return true;
        //			}

                    public boolean	visit(QualifiedName node) {
                        int line_number = unit.getLineNumber(node.getStartPosition());

                        tk.addHash(TokenType.QualifiedName.ordinal(), node.toString(), line_number);

                        return true;
                    }
/*
                    public boolean visit(AbstractTypeDeclaration node) {
                        System.out.println("AbstractTypeDeclaration");
                        return true;
                    }

                    public boolean visit(BodyDeclaration node) {
                        System.out.println("BodyDeclaration node");
                        return true;
                    }

                    public boolean	visit(QualifiedType node) {
                        System.out.println("QualifiedType");

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        System.out.println("QualifiedType at line "+ Integer.toString(line_number));

                        return true;
                    }
*/
                    public boolean	visit(ReturnStatement node) {
                        /*
                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);

                        Expression exp = node.getExpression();
                        if (exp == null) {
                            tk.addHash(48, line_number);
                            return true;
                        }
                        ITypeBinding bind = exp.resolveTypeBinding();
                        if (bind != null) {
                            String s = bind.getQualifiedName();
                            tk.addHash(48,s,line_number);
                        }
                        else {
                            tk.addHash(48, line_number);
                        }
                        */
                        return false;
                    }

                    /*
                    public void endVisit(ReturnStatement node) {
                        tk.statementEnd(currentLevel);
                    }
                    */

                    public boolean visit(SimpleName node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        String varType = TypeInfo.getMappedType(node.toString(), variableMap);
                        if (varType != null) {
                            //System.out.println(node);
                            tk.addHash(TokenType.SimpleName.ordinal(), varType, line_number);
                        } else {
                            tk.addHash(TokenType.SimpleName.ordinal(), node.toString(), line_number);
                        }

                        tk.insertSimpleName(node.toString());

                        return true;
                    }

                    /*
                    public boolean	visit(SimpleType node) {
                        System.out.println("ddd  " + node.toString());
                        int line_number = unit.getLineNumber(node.getStartPosition());
                        ITypeBinding typeBinding = node.resolveBinding();

                        if (typeBinding != null) {
                            tk.addHash(49, typeBinding.getQualifiedName(), line_number);
                        }
                        else {
                            tk.addHash(49, node.getName().getFullyQualifiedName(), line_number);
                        }

                        return true;
                    }*/
                    
                    public boolean	visit(SingleMemberAnnotation node) {
                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.SingleMemberAnnotation.ordinal(), node.getTypeName().toString(), line_number);				
                        
                        return true;
                    }

                    public boolean	visit(SingleVariableDeclaration node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        IVariableBinding binding = node.resolveBinding();
                        String varName = node.getName().toString();
                        if (binding != null) {
                            String variableType = binding.getType().getName();
                            tk.addHash(TokenType.SingleVariableDeclaration.ordinal(), variableType, line_number);

                            TypeInfo typeInfo = new TypeInfo(varName, currentLevel, variableType);
                            variableMap.add(typeInfo);

                        } else {
                            tk.addHash(TokenType.SingleVariableDeclaration.ordinal(), line_number);

                            TypeInfo typeInfo = new TypeInfo(varName, currentLevel, node.getType().toString());
                            variableMap.add(typeInfo);
                        }
                        return true;
                    }

                    public boolean	visit(StringLiteral node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        String strValue = node.getEscapedValue();
                        strValue = strValue.substring(1,strValue.length() - 1);
                        tk.addHash(TokenType.StringLiteral.ordinal(), strValue, line_number);

                        Set<String> termSet = Utilities.extractTermsFromSentence(strValue);
                        for (String word : termSet) {
                            tk.insertSimpleName(word);
                        }

                        return true;
                    }

                    public boolean	visit(SuperConstructorInvocation node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.SuperConstructorInvocation.ordinal(), line_number);

                        return true;
                    }

                    public void endVisit(SuperConstructorInvocation node) {
                        tk.statementEnd(currentLevel);
                    }

                    public boolean	visit(SuperFieldAccess node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.SuperFieldAccess.ordinal(), line_number);
                        return true;
                    }

                    public boolean	visit(SuperMethodInvocation node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.SuperMethodInvocation.ordinal(), node.getName().toString(), line_number);

                        /*
                        if (node.getExpression() != null) {
                            tk.addHash(86, node.getExpression().toString(), line_number);
                        }*/

                        return true;
                    }

                    /*
                    public boolean	visit(SwitchCase node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(54, line_number);
                        return true;
                    }*/

                    public boolean	visit(SwitchStatement node) {
                        /*

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);

                        tk.addHash(55, line_number);

                        tk.statementEnd(currentLevel);
                        */
                        return false;
                    }

                    public boolean	visit(SynchronizedStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);

                        tk.addHash(TokenType.SynchronizedStatement.ordinal(), line_number);

                        return true;
                    }

                    public boolean	visit(TagElement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.TagElement.ordinal(), line_number);
                        return true;
                    }

                    public boolean	visit(TextElement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.TextElement.ordinal(), line_number);

                        return true;
                    }

                    public boolean	visit(ThisExpression node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.addHash(TokenType.ThisExpression.ordinal(), line_number);
                        return true;
                    }

                    public boolean	visit(ThrowStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        tk.statementStart(line_number);
                        tk.addHash(TokenType.ThrowStatement.ordinal(), line_number);

                        return true;
                    }

                    public void endVisit(ThrowStatement node) {
                        tk.statementEnd(currentLevel);
                    }

                    public boolean	visit(TryStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        tk.statementStart(line_number);
                        tk.addHash(TokenType.TryStatement.ordinal(), line_number);
                        tk.statementEnd(currentLevel);

                        return true;
                    }
                    
                    public boolean	visit(TypeDeclaration node) {
                        //int line_number = unit.getLineNumber(node.getStartPosition());
                        //tk.statementStart(line_number);
                        //tk.addHash(62, line_number);
                        currentLevel = currentLevel + 1;
                        return true;
                    }

                    public void endVisit(TypeDeclaration node) {
                        
                        clearLocalVariables();

                        currentLevel = currentLevel - 1;       
                    }

                    /*
                    public boolean	visit(TypeDeclarationStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.statementEnd(currentLevel);

                        System.out.println("TypeDeclarationStatement at line "+ Integer.toString(line_number));

                        return true;
                    }*/

                    /*
                    public boolean	visit(TypeLiteral node) {

                        return true;
                    }

                    public boolean	visit(TypeParameter node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        //System.out.println("TypeParameter at line "+ Integer.toString(line_number));

                        return true;
                    }

                    public boolean	visit(UnionType node) {
                        System.out.println("uniontype");
                        return true;
                    }
                    */

                    public boolean visit(VariableDeclarationExpression node) {
                        //System.out.println("VariableDeclarationExpression");
                        int line_number = unit.getLineNumber(node.getStartPosition());

                        Type type = node.getType();
                        tk.addHash(TokenType.VariableDeclarationExpression.ordinal(), type.toString(), line_number);

                        List<VariableDeclarationFragment> fragList = node.fragments();
                        for (VariableDeclarationFragment frag : fragList) {
                            TypeInfo typeInfo = new TypeInfo(
                                    frag.getName().toString(), currentLevel, type.toString());
                            variableMap.add(typeInfo);
                        }

                        //System.out.println("VariableDeclarationExpression at line "+ Integer.toString(line_number));
                        return true;
                    }

                    public boolean visit(VariableDeclarationFragment node) {			   
                        //System.out.println("VariableDeclarationFragment");

                        int line_number = unit.getLineNumber(node.getStartPosition());

                        //System.out.println(node);
                        tk.addHash(TokenType.VariableDeclarationFragment.ordinal(), line_number);

                        return true;
                    }
                    
                    public boolean	visit(VariableDeclarationStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.VariableDeclarationStatement.ordinal(), line_number);

                        List<VariableDeclarationFragment> fragments = node.fragments();
                        for (VariableDeclarationFragment fragment : fragments) {
                            String varName = fragment.getName().toString();
                            String unresolvedType = node.getType().toString();   
                            TypeInfo typeInfo = new TypeInfo(varName, currentLevel, unresolvedType);
                            variableMap.add(typeInfo);
                        }

                        return true;
                    }

                    public void endVisit(VariableDeclarationStatement node) {
                        tk.statementEnd(currentLevel);
                    }
                    
                    public boolean	visit(WhileStatement node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.WhileStatement.ordinal(), line_number);

                        return true;
                    }

                    /*
                    public void endVisit(WhileStatement node) {
                        int line_number = unit.getLineNumber(node.getStartPosition()+node.getLength());
                        //tk.statementStart(line_number);
                        //tk.addHash(76, line_number);
                        //tk.statementEnd(currentLevel);
                    }*/

                    public boolean	visit(WildcardType node) {

                        int line_number = unit.getLineNumber(node.getStartPosition());
                        tk.statementStart(line_number);
                        tk.addHash(TokenType.WildcardType.ordinal(), line_number);
                        tk.statementEnd(currentLevel);
                        return true;
                    }
                    
                });
            } catch (Exception e) {
                System.out.println("Crashed while processing : " + absPath);
                System.out.println("Problem : " + e.toString());
                e.printStackTrace();
                System.exit(0);
            }
            return tk;

        } catch (Exception e) {
            System.out.println("\nError while executing compilation unit : " + e.toString());
            return null;
        }
    }
}
