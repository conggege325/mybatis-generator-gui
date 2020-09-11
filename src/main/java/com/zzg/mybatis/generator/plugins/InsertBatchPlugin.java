package com.zzg.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.config.GeneratedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 生成 mybatis 批量插入的插件
 *
 * @author zhangcong
 */
public class InsertBatchPlugin extends PluginAdapter {

    public InsertBatchPlugin() {
    }

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        AbstractXmlElementGenerator elementGenerator = new AbstractXmlElementGenerator() {
            @Override
            public void addElements(XmlElement parentElement) {
                XmlElement answer = new XmlElement("insert");

                answer.addAttribute(new Attribute("id", "insertBatch"));
                answer.addAttribute(new Attribute("parameterType", "list"));

                context.getCommentGenerator().addComment(answer);

                StringBuilder insertClause = new StringBuilder();

                insertClause.append("insert into ");
                insertClause.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
                insertClause.append(" (");

                XmlElement foreachXmlElement = new XmlElement("foreach");
                foreachXmlElement.addAttribute(new Attribute("collection", "list"));
                foreachXmlElement.addAttribute(new Attribute("item", "item"));
                foreachXmlElement.addAttribute(new Attribute("open", "values"));
                foreachXmlElement.addAttribute(new Attribute("separator", ","));

                StringBuilder valuesClause = new StringBuilder();
                valuesClause.append("(");

                List<String> valuesClauses = new ArrayList<String>();
                List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
                for (int i = 0; i < columns.size(); i++) {
                    IntrospectedColumn introspectedColumn = columns.get(i);

                    insertClause.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                    valuesClause.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "item."));
                    if (i + 1 < columns.size()) {
                        insertClause.append(", ");
                        valuesClause.append(", ");
                    }

                    if (valuesClause.length() > 80) {
                        answer.addElement(new TextElement(insertClause.toString()));
                        insertClause.setLength(0);
                        OutputUtilities.xmlIndent(insertClause, 1);

                        valuesClauses.add(valuesClause.toString());
                        valuesClause.setLength(0);
                        OutputUtilities.xmlIndent(valuesClause, 1);
                    }
                }

                insertClause.append(')');
                answer.addElement(new TextElement(insertClause.toString()));

                valuesClause.append(')');
                valuesClauses.add(valuesClause.toString());

                for (String clause : valuesClauses) {
                    foreachXmlElement.addElement(new TextElement(clause));
                }
                answer.addElement(foreachXmlElement);

                if (context.getPlugins().sqlMapInsertElementGenerated(answer, introspectedTable)) {
                    parentElement.addElement(answer);
                }
            }
        };
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.addElements(document.getRootElement());
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        AbstractJavaMapperMethodGenerator methodGenerator = new AbstractJavaMapperMethodGenerator() {
            @Override
            public void addInterfaceElements(Interface interfaze) {
                // 先创建import对象
                Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
                // 添加Lsit的包
                importedTypes.add(FullyQualifiedJavaType.getNewListInstance());

                // 创建方法对象
                Method method = new Method();
                // 设置该方法为public
                method.setVisibility(JavaVisibility.PUBLIC);
                // 设置返回类型
                method.setReturnType(FullyQualifiedJavaType.getIntInstance());
                // 设置方法名称
                method.setName("insertBatch");

                // 设置参数类型是对象
                FullyQualifiedJavaType parameterType = FullyQualifiedJavaType.getNewListInstance();
                parameterType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
                // 为方法添加参数
                method.addParameter(new Parameter(parameterType, "list"));

                context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
                if (context.getPlugins().clientSelectByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable)) {
                    interfaze.addImportedTypes(importedTypes);
                    interfaze.addMethod(method);
                }
            }
        };
        methodGenerator.setContext(context);
        methodGenerator.setIntrospectedTable(introspectedTable);
        methodGenerator.addInterfaceElements(interfaze);
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }
}
