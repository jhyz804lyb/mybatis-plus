package com.baomidou.mybatisplus.core;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class MybatisDefaultParameterHandlerTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Model {

        @TableId(type = IdType.INPUT)
        private Long id;

        private String name;

        private String insertOperator;

        @TableField(fill = FieldFill.UPDATE)
        private String updateOperator;

        Model(String name) {
            this.name = name;
        }

        Model(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test
    void test() {
        MappedStatement mappedStatement;
        Configuration configuration = new MybatisConfiguration();
        StaticSqlSource staticSqlSource = new StaticSqlSource(configuration, " ***********");
        GlobalConfigUtils.getGlobalConfig(configuration).setIdentifierGenerator(new DefaultIdentifierGenerator()).setMetaObjectHandler(new MetaObjectHandler() {

            @Override
            public boolean compatibleFillId() {
                return true;
            }

            @Override
            public void insertFill(MetaObject metaObject) {
                //TODO ????????????????????????????????????????????????????????????
                setFieldValByName("id", 666L, metaObject);
                setFieldValByName("insertOperator", "??????", metaObject);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                setFieldValByName("updateOperator", "??????", metaObject);
            }
        });
        Model model1 = new Model("??????");
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), Model.class);
        //??????
        mappedStatement = new MappedStatement.Builder(configuration, "***", staticSqlSource, SqlCommandType.SELECT).build();
        MybatisDefaultParameterHandler.processParameter(mappedStatement, model1);
        Assertions.assertNull(model1.getId());
        Assertions.assertNull(model1.getInsertOperator());
        Assertions.assertNull(model1.getUpdateOperator());
        // ????????????
        mappedStatement = new MappedStatement.Builder(configuration, "***", staticSqlSource, SqlCommandType.INSERT).build();
        MybatisDefaultParameterHandler.processParameter(mappedStatement, model1);
        Assertions.assertNotNull(model1.getId());
        Assertions.assertNotNull(model1.getInsertOperator());
        Assertions.assertNull(model1.getUpdateOperator());
        mappedStatement = new MappedStatement.Builder(configuration, "***", staticSqlSource, SqlCommandType.INSERT).build();
        //map??????
        Model model2 = new Model("??????");
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.ENTITY, model2);
        MybatisDefaultParameterHandler.processParameter(mappedStatement, params1);
        Assertions.assertNotNull(model2.getId());
        Assertions.assertNotNull(model2.getInsertOperator());
        Assertions.assertNull(model2.getUpdateOperator());
//        //map??????
//        Model model3 = new Model("??????");
//        Map<String, Object> params2 = new HashMap<>();
//        params2.put(Constants.ENTITY, new HashMap<String, Object>() {{
//            put(Constants.MP_OPTLOCK_ET_ORIGINAL, model3);
//        }});
//        MybatisDefaultParameterHandler.processParameter(mappedStatement, params2);
//        Assertions.assertNotNull(model3.getId());
//        Assertions.assertNotNull(model3.getInsertOperator());
//        Assertions.assertNull(model3.getUpdateOperator());
        //????????????
        Model model4 = new Model(1L,"??????");
        mappedStatement = new MappedStatement.Builder(configuration, "***", staticSqlSource, SqlCommandType.UPDATE).build();
        MybatisDefaultParameterHandler.processParameter(mappedStatement, model4);
        Assertions.assertNotNull(model4.getId());
        Assertions.assertNotNull(model4.getUpdateOperator());
        //????????????
        List<Model> list = Arrays.asList(new Model("????????????"), new Model("????????????"));
        mappedStatement = new MappedStatement.Builder(configuration, "***", staticSqlSource, SqlCommandType.INSERT).build();
        MybatisDefaultParameterHandler.processParameter(mappedStatement, list);
        list.forEach(m -> {
            Assertions.assertNotNull(m.getId());
            Assertions.assertNotNull(m.getInsertOperator());
            Assertions.assertNull(m.getUpdateOperator());
        });
        //????????????
        mappedStatement = new MappedStatement.Builder(configuration, "***", staticSqlSource, SqlCommandType.UPDATE).build();
        MybatisDefaultParameterHandler.processParameter(mappedStatement, list);
        list.forEach(m -> {
            Assertions.assertNotNull(m.getId());
            Assertions.assertNotNull(m.getInsertOperator());
            Assertions.assertNotNull(m.getUpdateOperator());
        });
    }

}
