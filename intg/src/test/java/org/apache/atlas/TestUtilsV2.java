/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas;

import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntity.AtlasEntitiesWithExtInfo;
import org.apache.atlas.model.instance.AtlasEntity.AtlasEntityWithExtInfo;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.instance.AtlasStruct;
import org.apache.atlas.model.typedef.AtlasBaseTypeDef;
import org.apache.atlas.model.typedef.AtlasClassificationDef;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.model.typedef.AtlasEnumDef;
import org.apache.atlas.model.typedef.AtlasEnumDef.AtlasEnumElementDef;
import org.apache.atlas.model.typedef.AtlasStructDef;
import org.apache.atlas.model.typedef.AtlasStructDef.AtlasAttributeDef;
import org.apache.atlas.model.typedef.AtlasStructDef.AtlasAttributeDef.Cardinality;
import org.apache.atlas.model.typedef.AtlasStructDef.AtlasConstraintDef;
import org.apache.atlas.model.typedef.AtlasTypesDef;
import org.apache.atlas.type.AtlasTypeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.atlas.type.AtlasTypeUtil.createStructTypeDef;


/**
 * Test utility class.
 */
public final class TestUtilsV2 {

    public static final long TEST_DATE_IN_LONG = 1418265358440L;

    public static final String TEST_USER = "testUser";

    private static AtomicInteger seq = new AtomicInteger();

    private TestUtilsV2() {
    }

    /**
     * Class Hierarchy is:
     * Department(name : String, employees : Array[Person])
     * Person(name : String, department : Department, manager : Manager)
     * Manager(subordinates : Array[Person]) extends Person
     * <p/>
     * Persons can have SecurityClearance(level : Int) clearance.
     */
    public static AtlasTypesDef defineDeptEmployeeTypes() {

        String _description = "_description";
        AtlasEnumDef orgLevelEnum =
                new AtlasEnumDef("OrgLevel", "OrgLevel"+_description, "1.0",
                        Arrays.asList(
                                new AtlasEnumElementDef("L1", "Element"+_description, 1),
                                new AtlasEnumElementDef("L2", "Element"+_description, 2)
                        ));

        AtlasStructDef addressDetails =
                createStructTypeDef("Address", "Address"+_description,
                        AtlasTypeUtil.createRequiredAttrDef("street", "string"),
                        AtlasTypeUtil.createRequiredAttrDef("city", "string"));

        AtlasEntityDef deptTypeDef =
                AtlasTypeUtil.createClassTypeDef(DEPARTMENT_TYPE, "Department"+_description, Collections.<String>emptySet(),
                        AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
                        new AtlasAttributeDef("employees", String.format("array<%s>", "Employee"), true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1, false, false,
                            new ArrayList<AtlasStructDef.AtlasConstraintDef>() {{
                                add(new AtlasStructDef.AtlasConstraintDef(AtlasConstraintDef.CONSTRAINT_TYPE_OWNED_REF));
                            }}));

        AtlasEntityDef personTypeDef = AtlasTypeUtil.createClassTypeDef("Person", "Person"+_description, Collections.<String>emptySet(),
                AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
                AtlasTypeUtil.createOptionalAttrDef("address", "Address"),
                AtlasTypeUtil.createOptionalAttrDef("birthday", "date"),
                AtlasTypeUtil.createOptionalAttrDef("hasPets", "boolean"),
                AtlasTypeUtil.createOptionalAttrDef("numberOfCars", "byte"),
                AtlasTypeUtil.createOptionalAttrDef("houseNumber", "short"),
                AtlasTypeUtil.createOptionalAttrDef("carMileage", "int"),
                AtlasTypeUtil.createOptionalAttrDef("age", "float"),
                AtlasTypeUtil.createOptionalAttrDef("numberOfStarsEstimate", "biginteger"),
                AtlasTypeUtil.createOptionalAttrDef("approximationOfPi", "bigdecimal")
        );

        AtlasEntityDef employeeTypeDef = AtlasTypeUtil.createClassTypeDef("Employee", "Employee"+_description, Collections.singleton("Person"),
                AtlasTypeUtil.createOptionalAttrDef("orgLevel", "OrgLevel"),
                new AtlasAttributeDef("department", "Department", false,
                        AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                        false, false,
                        new ArrayList<AtlasConstraintDef>()),
                new AtlasAttributeDef("manager", "Manager", true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                new ArrayList<AtlasConstraintDef>() {{
                        add(new AtlasConstraintDef(
                            AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF, new HashMap<String, Object>() {{
                            put(AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE, "subordinates");
                        }}));
                    }}),
                new AtlasAttributeDef("mentor", EMPLOYEE_TYPE, true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                AtlasTypeUtil.createOptionalAttrDef("shares", "long"),
                AtlasTypeUtil.createOptionalAttrDef("salary", "double")
                );

        employeeTypeDef.getAttribute("department").addConstraint(
            new AtlasConstraintDef(
                AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF, new HashMap<String, Object>() {{
                put(AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE, "employees");
            }}));

        AtlasEntityDef managerTypeDef = AtlasTypeUtil.createClassTypeDef("Manager", "Manager"+_description, Collections.singleton("Employee"),
                new AtlasAttributeDef("subordinates", String.format("array<%s>", "Employee"), false, AtlasAttributeDef.Cardinality.SET,
                        1, 10, false, false,
                        Collections.<AtlasConstraintDef>emptyList()));

        AtlasClassificationDef securityClearanceTypeDef =
                AtlasTypeUtil.createTraitTypeDef("SecurityClearance", "SecurityClearance"+_description, Collections.<String>emptySet(),
                        AtlasTypeUtil.createRequiredAttrDef("level", "int"));

        AtlasTypesDef ret = new AtlasTypesDef(Collections.singletonList(orgLevelEnum), Collections.singletonList(addressDetails),
                Collections.singletonList(securityClearanceTypeDef),
                Arrays.asList(deptTypeDef, personTypeDef, employeeTypeDef, managerTypeDef));

        populateSystemAttributes(ret);

        return ret;
    }

    public static AtlasTypesDef defineInverseReferenceTestTypes() {
        AtlasEntityDef aDef = AtlasTypeUtil.createClassTypeDef("A", Collections.<String>emptySet(),
            AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
            new AtlasAttributeDef("b", "B", true, Cardinality.SINGLE, 0, 1, false, false, Collections.<AtlasConstraintDef>emptyList()), // 1-1
            new AtlasAttributeDef("oneB", "B", true, Cardinality.SINGLE, 0, 1, false, false, Collections.<AtlasConstraintDef>emptyList()), // 1-*
            new AtlasAttributeDef("manyB", AtlasBaseTypeDef.getArrayTypeName("B"), true, Cardinality.SINGLE, 0, 1, false, false, Collections.<AtlasConstraintDef>emptyList()),
            new AtlasAttributeDef("mapToB", AtlasBaseTypeDef.getMapTypeName("string", "B"), true, Cardinality.SINGLE, 0, 1, false, false,
                Collections.<AtlasConstraintDef>singletonList(new AtlasConstraintDef(
                AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF, Collections.<String, Object>singletonMap(AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE, "mappedFromA"))))); // *-*

        AtlasEntityDef bDef = AtlasTypeUtil.createClassTypeDef("B", Collections.<String>emptySet(),
            AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
            new AtlasAttributeDef("a", "A", true, Cardinality.SINGLE, 0, 1, false, false,
                Collections.<AtlasConstraintDef>singletonList(new AtlasConstraintDef(
                    AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF, Collections.<String, Object>singletonMap(AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE, "b")))),
            new AtlasAttributeDef("manyA", AtlasBaseTypeDef.getArrayTypeName("A"), true, Cardinality.SINGLE, 0, 1, false, false,
                Collections.<AtlasConstraintDef>singletonList(new AtlasConstraintDef(
                    AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF, Collections.<String, Object>singletonMap(AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE, "oneB")))),
            new AtlasAttributeDef("manyToManyA", AtlasBaseTypeDef.getArrayTypeName("A"), true, Cardinality.SINGLE, 0, 1, false, false,
                Collections.<AtlasConstraintDef>singletonList(new AtlasConstraintDef(
                    AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF, Collections.<String, Object>singletonMap(AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE, "manyB")))),
            new AtlasAttributeDef("mappedFromA", "A", true, Cardinality.SINGLE, 0, 1, false, false, Collections.<AtlasConstraintDef>emptyList()));

        AtlasTypesDef ret = new AtlasTypesDef(Collections.<AtlasEnumDef>emptyList(), Collections.<AtlasStructDef>emptyList(), Collections.<AtlasClassificationDef>emptyList(), Arrays.asList(aDef, bDef));

        populateSystemAttributes(ret);

        return ret;
    }

    public static AtlasTypesDef defineValidUpdatedDeptEmployeeTypes() {
        String _description = "_description_updated";
        AtlasEnumDef orgLevelEnum =
                new AtlasEnumDef("OrgLevel", "OrgLevel"+_description, "1.0",
                        Arrays.asList(
                                new AtlasEnumElementDef("L1", "Element"+ _description, 1),
                                new AtlasEnumElementDef("L2", "Element"+ _description, 2)
                        ));

        AtlasStructDef addressDetails =
                createStructTypeDef("Address", "Address"+_description,
                        AtlasTypeUtil.createRequiredAttrDef("street", "string"),
                        AtlasTypeUtil.createRequiredAttrDef("city", "string"),
                        AtlasTypeUtil.createOptionalAttrDef("zip", "int"));

        AtlasEntityDef deptTypeDef =
                AtlasTypeUtil.createClassTypeDef(DEPARTMENT_TYPE, "Department"+_description,
                        Collections.<String>emptySet(),
                        AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
                        AtlasTypeUtil.createOptionalAttrDef("dep-code", "string"),
                        new AtlasAttributeDef("employees", String.format("array<%s>", "Employee"), true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1, false, false,
                            new ArrayList<AtlasStructDef.AtlasConstraintDef>() {{
                                add(new AtlasStructDef.AtlasConstraintDef(AtlasConstraintDef.CONSTRAINT_TYPE_OWNED_REF));
                            }}));

        AtlasEntityDef personTypeDef = AtlasTypeUtil.createClassTypeDef("Person", "Person"+_description,
                Collections.<String>emptySet(),
                AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
                AtlasTypeUtil.createOptionalAttrDef("email", "string"),
                AtlasTypeUtil.createOptionalAttrDef("address", "Address"),
                AtlasTypeUtil.createOptionalAttrDef("birthday", "date"),
                AtlasTypeUtil.createOptionalAttrDef("hasPets", "boolean"),
                AtlasTypeUtil.createOptionalAttrDef("numberOfCars", "byte"),
                AtlasTypeUtil.createOptionalAttrDef("houseNumber", "short"),
                AtlasTypeUtil.createOptionalAttrDef("carMileage", "int"),
                AtlasTypeUtil.createOptionalAttrDef("age", "float"),
                AtlasTypeUtil.createOptionalAttrDef("numberOfStarsEstimate", "biginteger"),
                AtlasTypeUtil.createOptionalAttrDef("approximationOfPi", "bigdecimal")
        );

        AtlasEntityDef employeeTypeDef = AtlasTypeUtil.createClassTypeDef("Employee", "Employee"+_description,
                Collections.singleton("Person"),
                AtlasTypeUtil.createOptionalAttrDef("orgLevel", "OrgLevel"),
                AtlasTypeUtil.createOptionalAttrDef("empCode", "string"),
                new AtlasAttributeDef("department", "Department", false,
                        AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("manager", "Manager", true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                    new ArrayList<AtlasConstraintDef>() {{
                        add(new AtlasConstraintDef(
                            AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF, new HashMap<String, Object>() {{
                            put(AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE, "subordinates");
                        }}));
                    }}),
                new AtlasAttributeDef("mentor", EMPLOYEE_TYPE, true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                AtlasTypeUtil.createOptionalAttrDef("shares", "long"),
                AtlasTypeUtil.createOptionalAttrDef("salary", "double")

        );

        AtlasEntityDef managerTypeDef = AtlasTypeUtil.createClassTypeDef("Manager", "Manager"+_description,
                Collections.singleton("Employee"),
                new AtlasAttributeDef("subordinates", String.format("array<%s>", "Employee"), false, AtlasAttributeDef.Cardinality.SET,
                        1, 10, false, false,
                        Collections.<AtlasConstraintDef>emptyList()));

        AtlasClassificationDef securityClearanceTypeDef =
                AtlasTypeUtil.createTraitTypeDef("SecurityClearance", "SecurityClearance"+_description, Collections.<String>emptySet(),
                        AtlasTypeUtil.createRequiredAttrDef("level", "int"));

        AtlasTypesDef ret = new AtlasTypesDef(Collections.singletonList(orgLevelEnum),
                Collections.singletonList(addressDetails),
                Collections.singletonList(securityClearanceTypeDef),
                Arrays.asList(deptTypeDef, personTypeDef, employeeTypeDef, managerTypeDef));

        populateSystemAttributes(ret);

        return ret;
    }

    public static AtlasTypesDef defineInvalidUpdatedDeptEmployeeTypes() {
        String _description = "_description_updated";
        // Test ordinal changes
        AtlasEnumDef orgLevelEnum =
                new AtlasEnumDef("OrgLevel", "OrgLevel"+_description, "1.0",
                        Arrays.asList(
                                new AtlasEnumElementDef("L2", "Element"+ _description, 1),
                                new AtlasEnumElementDef("L1", "Element"+ _description, 2),
                                new AtlasEnumElementDef("L3", "Element"+ _description, 3)
                        ));

        AtlasStructDef addressDetails =
                createStructTypeDef("Address", "Address"+_description,
                        AtlasTypeUtil.createRequiredAttrDef("street", "string"),
                        AtlasTypeUtil.createRequiredAttrDef("city", "string"),
                        AtlasTypeUtil.createRequiredAttrDef("zip", "int"));

        AtlasEntityDef deptTypeDef =
                AtlasTypeUtil.createClassTypeDef(DEPARTMENT_TYPE, "Department"+_description, Collections.<String>emptySet(),
                        AtlasTypeUtil.createRequiredAttrDef("name", "string"),
                        AtlasTypeUtil.createRequiredAttrDef("dep-code", "string"),
                        new AtlasAttributeDef("employees", String.format("array<%s>", "Person"), true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1, false, false,
                            new ArrayList<AtlasStructDef.AtlasConstraintDef>() {{
                                add(new AtlasStructDef.AtlasConstraintDef(AtlasConstraintDef.CONSTRAINT_TYPE_OWNED_REF));
                            }}));

        AtlasEntityDef personTypeDef = AtlasTypeUtil.createClassTypeDef("Person", "Person"+_description, Collections.<String>emptySet(),
                AtlasTypeUtil.createRequiredAttrDef("name", "string"),
                AtlasTypeUtil.createRequiredAttrDef("emp-code", "string"),
                AtlasTypeUtil.createOptionalAttrDef("orgLevel", "OrgLevel"),
                AtlasTypeUtil.createOptionalAttrDef("address", "Address"),
                new AtlasAttributeDef("department", "Department", false,
                        AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("manager", "Manager", true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                    new ArrayList<AtlasConstraintDef>() {{
                        add(new AtlasConstraintDef(
                            AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF, new HashMap<String, Object>() {{
                            put(AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE, "subordinates");
                        }}));
                    }}),
                new AtlasAttributeDef("mentor", "Person", true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                AtlasTypeUtil.createOptionalAttrDef("birthday", "date"),
                AtlasTypeUtil.createOptionalAttrDef("hasPets", "boolean"),
                AtlasTypeUtil.createOptionalAttrDef("numberOfCars", "byte"),
                AtlasTypeUtil.createOptionalAttrDef("houseNumber", "short"),
                AtlasTypeUtil.createOptionalAttrDef("carMileage", "int"),
                AtlasTypeUtil.createOptionalAttrDef("shares", "long"),
                AtlasTypeUtil.createOptionalAttrDef("salary", "double"),
                AtlasTypeUtil.createRequiredAttrDef("age", "float"),
                AtlasTypeUtil.createOptionalAttrDef("numberOfStarsEstimate", "biginteger"),
                AtlasTypeUtil.createOptionalAttrDef("approximationOfPi", "bigdecimal")
        );

        AtlasTypesDef ret = new AtlasTypesDef(Collections.singletonList(orgLevelEnum),
                                              Collections.singletonList(addressDetails),
                                              Collections.<AtlasClassificationDef>emptyList(),
                                              Arrays.asList(deptTypeDef, personTypeDef));

        populateSystemAttributes(ret);

        return ret;
    }

    public static final String DEPARTMENT_TYPE = "Department";
    public static final String EMPLOYEE_TYPE   = "Employee";
    public static final String MANAGER_TYPE    = "Manager";
    public static final String ADDRESS_TYPE    = "Address";

    public static AtlasEntitiesWithExtInfo createDeptEg2() {
        AtlasEntitiesWithExtInfo entitiesWithExtInfo = new AtlasEntitiesWithExtInfo();

        /******* Department - HR *******/
        AtlasEntity   hrDept   = new AtlasEntity(DEPARTMENT_TYPE, "name", "hr");
        AtlasObjectId hrDeptId = AtlasTypeUtil.getAtlasObjectId(hrDept);

        /******* Address Entities *******/
        AtlasStruct janeAddr = new AtlasStruct(ADDRESS_TYPE);
            janeAddr.setAttribute("street", "Great America Parkway");
            janeAddr.setAttribute("city", "Santa Clara");

        AtlasStruct juliusAddr = new AtlasStruct(ADDRESS_TYPE);
            juliusAddr.setAttribute("street", "Madison Ave");
            juliusAddr.setAttribute("city", "Newtonville");

        AtlasStruct maxAddr = new AtlasStruct(ADDRESS_TYPE);
            maxAddr.setAttribute("street", "Ripley St");
            maxAddr.setAttribute("city", "Newton");

        AtlasStruct johnAddr = new AtlasStruct(ADDRESS_TYPE);
            johnAddr.setAttribute("street", "Stewart Drive");
            johnAddr.setAttribute("city", "Sunnyvale");

        /******* Manager - Jane (John and Max subordinates) *******/
        AtlasEntity   jane   = new AtlasEntity(MANAGER_TYPE);
        AtlasObjectId janeId = AtlasTypeUtil.getAtlasObjectId(jane);
            jane.setAttribute("name", "Jane");
            jane.setAttribute("department", hrDeptId);
            jane.setAttribute("address", janeAddr);

        /******* Manager - Julius (no subordinates) *******/
        AtlasEntity   julius   = new AtlasEntity(MANAGER_TYPE);
        AtlasObjectId juliusId = AtlasTypeUtil.getAtlasObjectId(julius);
            julius.setAttribute("name", "Julius");
            julius.setAttribute("department", hrDeptId);
            julius.setAttribute("address", juliusAddr);
            julius.setAttribute("subordinates", Collections.emptyList());

        /******* Employee - Max (Manager: Jane, Mentor: Julius) *******/
        AtlasEntity   max   = new AtlasEntity(EMPLOYEE_TYPE);
        AtlasObjectId maxId = AtlasTypeUtil.getAtlasObjectId(max);
            max.setAttribute("name", "Max");
            max.setAttribute("department", hrDeptId);
            max.setAttribute("address", maxAddr);
            max.setAttribute("manager", janeId);
            max.setAttribute("mentor", juliusId);
            max.setAttribute("birthday",new Date(1979, 3, 15));
            max.setAttribute("hasPets", true);
            max.setAttribute("age", 36);
            max.setAttribute("numberOfCars", 2);
            max.setAttribute("houseNumber", 17);
            max.setAttribute("carMileage", 13);
            max.setAttribute("shares", Long.MAX_VALUE);
            max.setAttribute("salary", Double.MAX_VALUE);
            max.setAttribute("numberOfStarsEstimate", new BigInteger("1000000000000000000000000000000"));
            max.setAttribute("approximationOfPi", new BigDecimal("3.1415926535897932"));

        /******* Employee - John (Manager: Jane, Mentor: Max) *******/
        AtlasEntity   john   = new AtlasEntity(EMPLOYEE_TYPE);
        AtlasObjectId johnId = AtlasTypeUtil.getAtlasObjectId(john);
            john.setAttribute("name", "John");
            john.setAttribute("department", hrDeptId);
            john.setAttribute("address", johnAddr);
            john.setAttribute("manager", janeId);
            john.setAttribute("mentor", maxId);
            john.setAttribute("birthday",new Date(1950, 5, 15));
            john.setAttribute("hasPets", true);
            john.setAttribute("numberOfCars", 1);
            john.setAttribute("houseNumber", 153);
            john.setAttribute("carMileage", 13364);
            john.setAttribute("shares", 15000);
            john.setAttribute("salary", 123345.678);
            john.setAttribute("age", 50);
            john.setAttribute("numberOfStarsEstimate", new BigInteger("1000000000000000000000"));
            john.setAttribute("approximationOfPi", new BigDecimal("3.141592653589793238462643383279502884197169399375105820974944592307816406286"));

        jane.setAttribute("subordinates", Arrays.asList(johnId, maxId));
        hrDept.setAttribute("employees", Arrays.asList(janeId, juliusId, maxId, johnId));

        entitiesWithExtInfo.addEntity(hrDept);
        entitiesWithExtInfo.addEntity(jane);
        entitiesWithExtInfo.addEntity(julius);
        entitiesWithExtInfo.addEntity(max);
        entitiesWithExtInfo.addEntity(john);

        return entitiesWithExtInfo;
    }

    public static Map<String, AtlasEntity> createDeptEg1() {
        Map<String, AtlasEntity> deptEmpEntities = new HashMap<>();

        AtlasEntity hrDept = new AtlasEntity(DEPARTMENT_TYPE);
        AtlasEntity john = new AtlasEntity(EMPLOYEE_TYPE);

        AtlasEntity jane = new AtlasEntity("Manager");
        AtlasEntity johnAddr = new AtlasEntity("Address");
        AtlasEntity janeAddr = new AtlasEntity("Address");
        AtlasEntity julius = new AtlasEntity("Manager");
        AtlasEntity juliusAddr = new AtlasEntity("Address");
        AtlasEntity max = new AtlasEntity(EMPLOYEE_TYPE);
        AtlasEntity maxAddr = new AtlasEntity("Address");

        AtlasObjectId deptId = new AtlasObjectId(hrDept.getGuid(), hrDept.getTypeName());
        hrDept.setAttribute("name", "hr");
        john.setAttribute("name", "John");
        john.setAttribute("department", deptId);
        johnAddr.setAttribute("street", "Stewart Drive");
        johnAddr.setAttribute("city", "Sunnyvale");
        john.setAttribute("address", johnAddr);

        john.setAttribute("birthday",new Date(1950, 5, 15));
        john.setAttribute("hasPets", true);
        john.setAttribute("numberOfCars", 1);
        john.setAttribute("houseNumber", 153);
        john.setAttribute("carMileage", 13364);
        john.setAttribute("shares", 15000);
        john.setAttribute("salary", 123345.678);
        john.setAttribute("age", 50);
        john.setAttribute("numberOfStarsEstimate", new BigInteger("1000000000000000000000"));
        john.setAttribute("approximationOfPi", new BigDecimal("3.141592653589793238462643383279502884197169399375105820974944592307816406286"));

        jane.setAttribute("name", "Jane");
        jane.setAttribute("department", deptId);
        janeAddr.setAttribute("street", "Great America Parkway");
        janeAddr.setAttribute("city", "Santa Clara");
        jane.setAttribute("address", janeAddr);
        janeAddr.setAttribute("street", "Great America Parkway");

        julius.setAttribute("name", "Julius");
        julius.setAttribute("department", deptId);
        juliusAddr.setAttribute("street", "Madison Ave");
        juliusAddr.setAttribute("city", "Newtonville");
        julius.setAttribute("address", juliusAddr);
        julius.setAttribute("subordinates", Collections.emptyList());

        AtlasObjectId janeId = AtlasTypeUtil.getAtlasObjectId(jane);
        AtlasObjectId johnId = AtlasTypeUtil.getAtlasObjectId(john);

        //TODO - Change to MANAGER_TYPE for JULIUS
        AtlasObjectId maxId = new AtlasObjectId(max.getGuid(), EMPLOYEE_TYPE);
        AtlasObjectId juliusId = new AtlasObjectId(julius.getGuid(), EMPLOYEE_TYPE);

        max.setAttribute("name", "Max");
        max.setAttribute("department", deptId);
        maxAddr.setAttribute("street", "Ripley St");
        maxAddr.setAttribute("city", "Newton");
        max.setAttribute("address", maxAddr);
        max.setAttribute("manager", janeId);
        max.setAttribute("mentor", juliusId);
        max.setAttribute("birthday",new Date(1979, 3, 15));
        max.setAttribute("hasPets", true);
        max.setAttribute("age", 36);
        max.setAttribute("numberOfCars", 2);
        max.setAttribute("houseNumber", 17);
        max.setAttribute("carMileage", 13);
        max.setAttribute("shares", Long.MAX_VALUE);
        max.setAttribute("salary", Double.MAX_VALUE);
        max.setAttribute("numberOfStarsEstimate", new BigInteger("1000000000000000000000000000000"));
        max.setAttribute("approximationOfPi", new BigDecimal("3.1415926535897932"));

        john.setAttribute("manager", janeId);
        john.setAttribute("mentor", maxId);
        hrDept.setAttribute("employees", Arrays.asList(johnId, janeId, juliusId, maxId));

        jane.setAttribute("subordinates", Arrays.asList(johnId, maxId));

        deptEmpEntities.put(jane.getGuid(), jane);
        deptEmpEntities.put(john.getGuid(), john);
        deptEmpEntities.put(julius.getGuid(), julius);
        deptEmpEntities.put(max.getGuid(), max);
        deptEmpEntities.put(deptId.getGuid(), hrDept);
        return deptEmpEntities;
    }

    public static final String DATABASE_TYPE = "hive_database";
    public static final String DATABASE_NAME = "foo";
    public static final String TABLE_TYPE = "hive_table";
    public static final String PROCESS_TYPE = "hive_process";
    public static final String COLUMN_TYPE = "column_type";
    public static final String TABLE_NAME = "bar";
    public static final String CLASSIFICATION = "classification";
    public static final String PII = "PII";
    public static final String PHI = "PHI";
    public static final String SUPER_TYPE_NAME = "Base";
    public static final String STORAGE_DESC_TYPE = "hive_storagedesc";
    public static final String PARTITION_STRUCT_TYPE = "partition_struct_type";
    public static final String PARTITION_CLASS_TYPE = "partition_class_type";
    public static final String SERDE_TYPE = "serdeType";
    public static final String COLUMNS_MAP = "columnsMap";
    public static final String COLUMNS_ATTR_NAME = "columns";
    public static final String ENTITY_TYPE_WITH_NESTED_COLLECTION_ATTR = "entity_with_nested_collection_attr";

    public static final String NAME = "name";

    public static AtlasTypesDef simpleType(){
        AtlasEntityDef superTypeDefinition =
                AtlasTypeUtil.createClassTypeDef("h_type", Collections.<String>emptySet(),
                        AtlasTypeUtil.createOptionalAttrDef("attr", "string"));

        AtlasStructDef structTypeDefinition = new AtlasStructDef("s_type", "structType", "1.0",
                Arrays.asList(AtlasTypeUtil.createRequiredAttrDef("name", "string")));

        AtlasClassificationDef traitTypeDefinition =
                AtlasTypeUtil.createTraitTypeDef("t_type", "traitType", Collections.<String>emptySet());

        AtlasEnumDef enumTypeDefinition = new AtlasEnumDef("e_type", "enumType", "1.0",
                Arrays.asList(new AtlasEnumElementDef("ONE", "Element Description", 1)));

        AtlasTypesDef ret = AtlasTypeUtil.getTypesDef(Collections.singletonList(enumTypeDefinition), Collections.singletonList(structTypeDefinition),
                Collections.singletonList(traitTypeDefinition), Collections.singletonList(superTypeDefinition));

        populateSystemAttributes(ret);

        return ret;
    }

    public static AtlasTypesDef simpleTypeUpdated(){
        AtlasEntityDef superTypeDefinition =
                AtlasTypeUtil.createClassTypeDef("h_type", Collections.<String>emptySet(),
                        AtlasTypeUtil.createOptionalAttrDef("attr", "string"));

        AtlasEntityDef newSuperTypeDefinition =
                AtlasTypeUtil.createClassTypeDef("new_h_type", Collections.<String>emptySet(),
                        AtlasTypeUtil.createOptionalAttrDef("attr", "string"));

        AtlasStructDef structTypeDefinition = new AtlasStructDef("s_type", "structType", "1.0",
                Arrays.asList(AtlasTypeUtil.createRequiredAttrDef("name", "string")));

        AtlasClassificationDef traitTypeDefinition =
                AtlasTypeUtil.createTraitTypeDef("t_type", "traitType", Collections.<String>emptySet());

        AtlasEnumDef enumTypeDefinition = new AtlasEnumDef("e_type", "enumType",
                Arrays.asList(new AtlasEnumElementDef("ONE", "Element Description", 1)));
        AtlasTypesDef ret = AtlasTypeUtil.getTypesDef(Collections.singletonList(enumTypeDefinition), Collections.singletonList(structTypeDefinition),
                Collections.singletonList(traitTypeDefinition), Arrays.asList(superTypeDefinition, newSuperTypeDefinition));

        populateSystemAttributes(ret);

        return ret;
    }

    public static AtlasTypesDef simpleTypeUpdatedDiff() {
        AtlasEntityDef newSuperTypeDefinition =
                AtlasTypeUtil.createClassTypeDef("new_h_type", Collections.<String>emptySet(),
                        AtlasTypeUtil.createOptionalAttrDef("attr", "string"));

        AtlasTypesDef ret = AtlasTypeUtil.getTypesDef(Collections.<AtlasEnumDef>emptyList(),
                Collections.<AtlasStructDef>emptyList(),
                Collections.<AtlasClassificationDef>emptyList(),
                Collections.singletonList(newSuperTypeDefinition));

        populateSystemAttributes(ret);

        return ret;
    }


    public static AtlasTypesDef defineHiveTypes() {
        String _description = "_description";
        AtlasEntityDef superTypeDefinition =
                AtlasTypeUtil.createClassTypeDef(SUPER_TYPE_NAME, "SuperType_description", Collections.<String>emptySet(),
                        AtlasTypeUtil.createOptionalAttrDef("namespace", "string"),
                        AtlasTypeUtil.createOptionalAttrDef("cluster", "string"),
                        AtlasTypeUtil.createOptionalAttrDef("colo", "string"));
        AtlasEntityDef databaseTypeDefinition =
                AtlasTypeUtil.createClassTypeDef(DATABASE_TYPE, DATABASE_TYPE + _description,Collections.singleton(SUPER_TYPE_NAME),
                        AtlasTypeUtil.createUniqueRequiredAttrDef(NAME, "string"),
                        AtlasTypeUtil.createOptionalAttrDef("isReplicated", "boolean"),
                        AtlasTypeUtil.createOptionalAttrDef("created", "string"),
                        AtlasTypeUtil.createOptionalAttrDef("parameters", "map<string,string>"),
                        AtlasTypeUtil.createRequiredAttrDef("description", "string"));


        AtlasStructDef structTypeDefinition = new AtlasStructDef("serdeType", "serdeType" + _description, "1.0",
                Arrays.asList(
                        AtlasTypeUtil.createRequiredAttrDef("name", "string"),
                        AtlasTypeUtil.createRequiredAttrDef("serde", "string"),
                        AtlasTypeUtil.createOptionalAttrDef("description", "string")));

        AtlasEnumElementDef values[] = {
                new AtlasEnumElementDef("MANAGED", "Element Description", 1),
                new AtlasEnumElementDef("EXTERNAL", "Element Description", 2)};

        AtlasEnumDef enumTypeDefinition = new AtlasEnumDef("tableType", "tableType" + _description, "1.0", Arrays.asList(values));

        AtlasEntityDef columnsDefinition =
                AtlasTypeUtil.createClassTypeDef(COLUMN_TYPE, COLUMN_TYPE + "_description",
                        Collections.<String>emptySet(),
                        AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
                        AtlasTypeUtil.createRequiredAttrDef("type", "string"),
                        AtlasTypeUtil.createOptionalAttrDef("description", "string"),
                        new AtlasAttributeDef("table", TABLE_TYPE,
                        true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        new ArrayList<AtlasStructDef.AtlasConstraintDef>() {{
                            add(new AtlasStructDef.AtlasConstraintDef(
                                AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF, new HashMap<String, Object>() {{
                                put(AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE, "columns");
                            }}));
                        }})
                        );

        AtlasStructDef partitionDefinition = new AtlasStructDef("partition_struct_type", "partition_struct_type" + _description, "1.0",
                Arrays.asList(AtlasTypeUtil.createRequiredAttrDef("name", "string")));

        AtlasAttributeDef[] attributeDefinitions = new AtlasAttributeDef[]{
                new AtlasAttributeDef("location", "string", true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("inputFormat", "string", true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("outputFormat", "string", true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("compressed", "boolean", false,
                        AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("numBuckets", "int", true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
        };

        AtlasEntityDef storageDescClsDef =
                new AtlasEntityDef(STORAGE_DESC_TYPE, STORAGE_DESC_TYPE + _description, "1.0",
                        Arrays.asList(attributeDefinitions), Collections.singleton(SUPER_TYPE_NAME));

        AtlasAttributeDef[] partClsAttributes = new AtlasAttributeDef[]{
                new AtlasAttributeDef("values", "array<string>",
                        true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("table", TABLE_TYPE, true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("createTime", "long", true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("lastAccessTime", "long", true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("sd", STORAGE_DESC_TYPE, false,
                        AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("columns", String.format("array<%s>", COLUMN_TYPE),
                        true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList()),
                new AtlasAttributeDef("parameters", String.format("map<%s,%s>", "string", "string"), true,
                        AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                        false, false,
                        Collections.<AtlasConstraintDef>emptyList())};

        AtlasEntityDef partClsDef =
                new AtlasEntityDef("partition_class_type", "partition_class_type" + _description, "1.0",
                        Arrays.asList(partClsAttributes), Collections.singleton(SUPER_TYPE_NAME));

        AtlasEntityDef processClsType =
                new AtlasEntityDef(PROCESS_TYPE, PROCESS_TYPE + _description, "1.0",
                        Arrays.asList(new AtlasAttributeDef("outputs", "array<" + TABLE_TYPE + ">", true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList())),
                        Collections.<String>emptySet());

        AtlasEntityDef tableTypeDefinition =
                AtlasTypeUtil.createClassTypeDef(TABLE_TYPE, TABLE_TYPE + _description, Collections.singleton(SUPER_TYPE_NAME),
                        AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
                        AtlasTypeUtil.createOptionalAttrDef("description", "string"),
                        AtlasTypeUtil.createRequiredAttrDef("type", "string"),
                        AtlasTypeUtil.createOptionalAttrDef("created", "date"),
                        // enum
                        new AtlasAttributeDef("tableType", "tableType", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        // array of strings
                        new AtlasAttributeDef("columnNames",
                                String.format("array<%s>", "string"), true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        // array of classes
                        new AtlasAttributeDef("columns", String.format("array<%s>", COLUMN_TYPE),
                                true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                                false, false,
                                new ArrayList<AtlasStructDef.AtlasConstraintDef>() {{
                                    add(new AtlasStructDef.AtlasConstraintDef(AtlasConstraintDef.CONSTRAINT_TYPE_OWNED_REF));
                                }}),
                        // array of structs
                        new AtlasAttributeDef("partitions", String.format("array<%s>", "partition_struct_type"),
                                true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        // map of primitives
                        new AtlasAttributeDef("parametersMap", String.format("map<%s,%s>", "string", "string"),
                                true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        //map of classes -
                        new AtlasAttributeDef(COLUMNS_MAP,
                                String.format("map<%s,%s>", "string", COLUMN_TYPE),
                                true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                                false, false,
                                 new ArrayList<AtlasStructDef.AtlasConstraintDef>() {{
                                     add(new AtlasStructDef.AtlasConstraintDef(
                                             AtlasConstraintDef.CONSTRAINT_TYPE_OWNED_REF));
                                     }}
                                ),
                        //map of structs
                        new AtlasAttributeDef("partitionsMap",
                                String.format("map<%s,%s>", "string", "partition_struct_type"),
                                true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        // struct reference
                        new AtlasAttributeDef("serde1", "serdeType", true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("serde2", "serdeType", true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        // class reference
                        new AtlasAttributeDef("database", DATABASE_TYPE, false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        //class reference as composite
                        new AtlasAttributeDef("databaseComposite", DATABASE_TYPE, true,
                                AtlasAttributeDef.Cardinality.SINGLE, 0, 1,
                                false, false,
                                new ArrayList<AtlasStructDef.AtlasConstraintDef>() {{
                                    add(new AtlasStructDef.AtlasConstraintDef(
                                            AtlasConstraintDef.CONSTRAINT_TYPE_OWNED_REF));
                                }}
                        ));

        AtlasClassificationDef piiTypeDefinition =
                AtlasTypeUtil.createTraitTypeDef(PII, PII + _description, Collections.<String>emptySet());

        AtlasClassificationDef classificationTypeDefinition =
                AtlasTypeUtil.createTraitTypeDef(CLASSIFICATION, CLASSIFICATION + _description, Collections.<String>emptySet(),
                        AtlasTypeUtil.createRequiredAttrDef("tag", "string"));

        AtlasClassificationDef fetlClassificationTypeDefinition =
                AtlasTypeUtil.createTraitTypeDef("fetl" + CLASSIFICATION, "fetl" + CLASSIFICATION + _description, Collections.singleton(CLASSIFICATION),
                        AtlasTypeUtil.createRequiredAttrDef("tag", "string"));

        AtlasClassificationDef phiTypeDefinition = AtlasTypeUtil.createTraitTypeDef(PHI, PHI + _description, Collections.<String>emptySet(),
                                                                                    AtlasTypeUtil.createRequiredAttrDef("stringAttr", "string"),
                                                                                    AtlasTypeUtil.createRequiredAttrDef("booleanAttr", "boolean"),
                                                                                    AtlasTypeUtil.createRequiredAttrDef("integerAttr", "int"));

        AtlasTypesDef ret = AtlasTypeUtil.getTypesDef(Collections.singletonList(enumTypeDefinition),
                                                      Arrays.asList(structTypeDefinition, partitionDefinition),
                                                      Arrays.asList(classificationTypeDefinition, fetlClassificationTypeDefinition, piiTypeDefinition, phiTypeDefinition),
                                                      Arrays.asList(superTypeDefinition, databaseTypeDefinition, columnsDefinition, tableTypeDefinition, storageDescClsDef, partClsDef, processClsType));

        populateSystemAttributes(ret);

        return ret;
    }

    public static AtlasTypesDef defineTypeWithNestedCollectionAttributes() {
        AtlasEntityDef nestedCollectionAttributesEntityType =
                AtlasTypeUtil.createClassTypeDef(ENTITY_TYPE_WITH_NESTED_COLLECTION_ATTR, ENTITY_TYPE_WITH_NESTED_COLLECTION_ATTR + "_description", null,
                        AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),

                        new AtlasAttributeDef("mapOfArrayOfStrings", "map<string,array<string>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("mapOfArrayOfBooleans", "map<string,array<boolean>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("mapOfArrayOfInts", "map<string,array<int>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("mapOfArrayOfFloats", "map<string,array<float>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("mapOfArrayOfDates", "map<string,array<date>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),

                        new AtlasAttributeDef("mapOfMapOfStrings", "map<string,map<string,string>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("mapOfMapOfBooleans", "map<string,map<string,boolean>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("mapOfMapOfInts", "map<string,map<string,int>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("mapOfMapOfFloats", "map<string,map<string,float>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("mapOfMapOfDates", "map<string,map<string,date>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),

                        new AtlasAttributeDef("arrayOfArrayOfStrings", "array<array<string>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("arrayOfArrayOfBooleans", "array<array<boolean>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("arrayOfArrayOfInts", "array<array<int>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("arrayOfArrayOfFloats", "array<array<float>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("arrayOfArrayOfDates", "array<array<date>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),

                        new AtlasAttributeDef("arrayOfMapOfStrings", "array<map<string,string>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("arrayOfMapOfBooleans", "array<map<string,boolean>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("arrayOfMapOfInts", "array<map<string,int>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("arrayOfMapOfFloats", "array<map<string,float>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList()),
                        new AtlasAttributeDef("arrayOfMapOfDates", "array<map<string,date>>", false,
                                AtlasAttributeDef.Cardinality.SINGLE, 1, 1,
                                false, false,
                                Collections.<AtlasConstraintDef>emptyList())
                );

        AtlasTypesDef ret = AtlasTypeUtil.getTypesDef(Collections.emptyList(),
                                                      Collections.emptyList(),
                                                      Collections.emptyList(),
                                                      Arrays.asList(nestedCollectionAttributesEntityType));

        populateSystemAttributes(ret);

        return ret;
    }

    public static AtlasEntityWithExtInfo createNestedCollectionAttrEntity() {
        AtlasEntity entity = new AtlasEntity(ENTITY_TYPE_WITH_NESTED_COLLECTION_ATTR);

        String[]  arrayOfStrings  = new String[] { "one", "two", "three" };
        boolean[] arrayOfBooleans = new boolean[] { false, true };
        int[]     arrayOfInts     = new int[] { 1, 2, 3 };
        float[]   arrayOfFloats   = new float[] { 1.1f, 2.2f, 3.3f };
        Date[]    arrayOfDates    = new Date[] { new Date() };

        Map<String, String>  mapOfStrings  = Collections.singletonMap("one", "one");
        Map<String, Boolean> mapOfBooleans = Collections.singletonMap("one", true);
        Map<String, Integer> mapOfInts     = Collections.singletonMap("one", 1);
        Map<String, Float>   mapOfFloats   = Collections.singletonMap("one", 1.1f);
        Map<String, Date>    mapOfDates    = Collections.singletonMap("now", new Date());

        entity.setAttribute("name", randomString() + "_" + System.currentTimeMillis());

        entity.setAttribute("mapOfArrayOfStrings", Collections.singletonMap("one", arrayOfStrings));
        entity.setAttribute("mapOfArrayOfBooleans", Collections.singletonMap("one", arrayOfBooleans));
        entity.setAttribute("mapOfArrayOfInts", Collections.singletonMap("one", arrayOfInts));
        entity.setAttribute("mapOfArrayOfFloats", Collections.singletonMap("one", arrayOfFloats));
        entity.setAttribute("mapOfArrayOfDates", Collections.singletonMap("one", arrayOfDates));

        entity.setAttribute("mapOfMapOfStrings", Collections.singletonMap("one", mapOfStrings));
        entity.setAttribute("mapOfMapOfBooleans", Collections.singletonMap("one", mapOfBooleans));
        entity.setAttribute("mapOfMapOfInts", Collections.singletonMap("one", mapOfInts));
        entity.setAttribute("mapOfMapOfFloats", Collections.singletonMap("one", mapOfFloats));
        entity.setAttribute("mapOfMapOfDates", Collections.singletonMap("one", mapOfDates));

        entity.setAttribute("arrayOfArrayOfStrings", Collections.singletonList(arrayOfStrings));
        entity.setAttribute("arrayOfArrayOfBooleans", Collections.singletonList(arrayOfBooleans));
        entity.setAttribute("arrayOfArrayOfInts", Collections.singletonList(arrayOfInts));
        entity.setAttribute("arrayOfArrayOfFloats", Collections.singletonList(arrayOfFloats));
        entity.setAttribute("arrayOfArrayOfDates", Collections.singletonList(arrayOfDates));

        entity.setAttribute("arrayOfMapOfStrings", Collections.singletonList(mapOfStrings));
        entity.setAttribute("arrayOfMapOfBooleans", Collections.singletonList(mapOfBooleans));
        entity.setAttribute("arrayOfMapOfInts", Collections.singletonList(mapOfInts));
        entity.setAttribute("arrayOfMapOfFloats", Collections.singletonList(mapOfFloats));
        entity.setAttribute("arrayOfMapOfDates", Collections.singletonList(mapOfDates));

        return new AtlasEntityWithExtInfo(entity);
    }

    public static final String randomString() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    public static final String randomString(int count) {
        return RandomStringUtils.randomAlphanumeric(count);
    }

    public static AtlasEntity createDBEntity() {
        String dbName = RandomStringUtils.randomAlphanumeric(10);
        return createDBEntity(dbName);
    }

    public static AtlasEntity createDBEntity(String dbName) {
        AtlasEntity entity = new AtlasEntity(DATABASE_TYPE);
        entity.setAttribute(NAME, dbName);
        entity.setAttribute("description", "us db");

        return entity;
    }

    public static AtlasEntityWithExtInfo createDBEntityV2() {
        AtlasEntity dbEntity = new AtlasEntity(DATABASE_TYPE);

        dbEntity.setAttribute(NAME, RandomStringUtils.randomAlphanumeric(10));
        dbEntity.setAttribute("description", "us db");

        return new AtlasEntityWithExtInfo(dbEntity);
    }

    public static AtlasEntity createTableEntity(AtlasEntity dbEntity) {
        String tableName = RandomStringUtils.randomAlphanumeric(10);
        return createTableEntity(dbEntity, tableName);
    }

    public static AtlasEntity createTableEntity(AtlasEntity dbEntity, String name) {
        AtlasEntity entity = new AtlasEntity(TABLE_TYPE);
        entity.setAttribute(NAME, name);
        entity.setAttribute("description", "random table");
        entity.setAttribute("type", "type");
        entity.setAttribute("tableType", "MANAGED");
        entity.setAttribute("database", AtlasTypeUtil.getAtlasObjectId(dbEntity));
        entity.setAttribute("created", new Date());

        Map<String, Object> partAttributes = new HashMap<String, Object>() {{
            put("name", "part0");
        }};
        final AtlasStruct partitionStruct  = new AtlasStruct("partition_struct_type", partAttributes);

        entity.setAttribute("partitions", new ArrayList<AtlasStruct>() {{ add(partitionStruct); }});
        entity.setAttribute("parametersMap", new java.util.HashMap<String, String>() {{
            put("key1", "value1");
        }});

        return entity;
    }

    public static AtlasEntityWithExtInfo createTableEntityV2(AtlasEntity dbEntity) {
        AtlasEntity tblEntity = new AtlasEntity(TABLE_TYPE);

        tblEntity.setAttribute(NAME, RandomStringUtils.randomAlphanumeric(10));
        tblEntity.setAttribute("description", "random table");
        tblEntity.setAttribute("type", "type");
        tblEntity.setAttribute("tableType", "MANAGED");
        tblEntity.setAttribute("database", AtlasTypeUtil.getAtlasObjectId(dbEntity));
        tblEntity.setAttribute("created", new Date());

        final AtlasStruct partitionStruct = new AtlasStruct("partition_struct_type", "name", "part0");

        tblEntity.setAttribute("partitions", new ArrayList<AtlasStruct>() {{ add(partitionStruct); }});
        tblEntity.setAttribute("parametersMap",
                new java.util.HashMap<String, String>() {{ put("key1", "value1"); }});


        AtlasEntityWithExtInfo ret = new AtlasEntityWithExtInfo(tblEntity);

        ret.addReferredEntity(dbEntity);

        return ret;
    }

    public static AtlasEntityWithExtInfo createprimitiveEntityV2() {

        AtlasEntity defaultprimitive = new AtlasEntity(createPrimitiveEntityDef());
        defaultprimitive.setAttribute("name", "testname");
        defaultprimitive.setAttribute("description","test");
        defaultprimitive.setAttribute("check","check");

        AtlasEntityWithExtInfo ret = new AtlasEntityWithExtInfo(defaultprimitive);

        return ret;

    }


    public static AtlasEntityDef createPrimitiveEntityDef() {

        AtlasEntityDef newtestEntityDef = new AtlasEntityDef("newtest");
        AtlasAttributeDef attrName = new AtlasAttributeDef("name", AtlasBaseTypeDef.ATLAS_TYPE_STRING);

        AtlasAttributeDef attrDescription = new AtlasAttributeDef("description", AtlasBaseTypeDef.ATLAS_TYPE_STRING);
        attrDescription.setIsOptional(false);

        AtlasAttributeDef attrcheck = new AtlasAttributeDef("check", AtlasBaseTypeDef.ATLAS_TYPE_STRING);
        attrcheck.setIsOptional(true);

        AtlasAttributeDef attrSourceCode = new AtlasAttributeDef("sourcecode", AtlasBaseTypeDef.ATLAS_TYPE_STRING);
        attrSourceCode.setDefaultValue("Hello World");
        attrSourceCode.setIsOptional(true);

        AtlasAttributeDef attrCost = new AtlasAttributeDef("Cost", AtlasBaseTypeDef.ATLAS_TYPE_INT);
        attrCost.setIsOptional(true);
        attrCost.setDefaultValue("30");

        AtlasAttributeDef attrDiskUsage = new AtlasAttributeDef("diskUsage", AtlasBaseTypeDef.ATLAS_TYPE_FLOAT);
        attrDiskUsage.setIsOptional(true);
        attrDiskUsage.setDefaultValue("70.50");

        AtlasAttributeDef attrisStoreUse = new AtlasAttributeDef("isstoreUse", AtlasBaseTypeDef.ATLAS_TYPE_BOOLEAN);
        attrisStoreUse.setIsOptional(true);
        attrisStoreUse.setDefaultValue("true");

        newtestEntityDef.addAttribute(attrName);
        newtestEntityDef.addAttribute(attrDescription);
        newtestEntityDef.addAttribute(attrcheck);
        newtestEntityDef.addAttribute(attrSourceCode);
        newtestEntityDef.addAttribute(attrCost);
        newtestEntityDef.addAttribute(attrDiskUsage);
        newtestEntityDef.addAttribute(attrisStoreUse);

        populateSystemAttributes(newtestEntityDef);

        return newtestEntityDef;
    }

    public static AtlasEntity createColumnEntity(AtlasEntity tableEntity) {
        return createColumnEntity(tableEntity, "col" + seq.addAndGet(1));
    }

    public static AtlasEntity createColumnEntity(AtlasEntity tableEntity, String colName) {
        AtlasEntity entity = new AtlasEntity(COLUMN_TYPE);
        entity.setAttribute(NAME, colName);
        entity.setAttribute("type", "VARCHAR(32)");
        entity.setAttribute("table", AtlasTypeUtil.getAtlasObjectId(tableEntity));
        return entity;
    }

    public static AtlasEntity createProcessEntity(List<AtlasObjectId> inputs, List<AtlasObjectId> outputs) {

        AtlasEntity entity = new AtlasEntity(PROCESS_TYPE);
        entity.setAttribute(NAME, RandomStringUtils.randomAlphanumeric(10));
        entity.setAttribute("inputs", inputs);
        entity.setAttribute("outputs", outputs);
        return entity;
    }

    public static List<AtlasClassificationDef> getClassificationWithValidSuperType() {
        AtlasClassificationDef securityClearanceTypeDef =
                AtlasTypeUtil.createTraitTypeDef("SecurityClearance1", "SecurityClearance_description", Collections.<String>emptySet(),
                        AtlasTypeUtil.createRequiredAttrDef("level", "int"));

        AtlasClassificationDef janitorSecurityClearanceTypeDef =
                AtlasTypeUtil.createTraitTypeDef("JanitorClearance", "JanitorClearance_description", Collections.singleton("SecurityClearance1"),
                        AtlasTypeUtil.createRequiredAttrDef("level", "int"));

        List<AtlasClassificationDef> ret = Arrays.asList(securityClearanceTypeDef, janitorSecurityClearanceTypeDef);

        populateSystemAttributes(ret);

        return ret;
    }

    public static List<AtlasClassificationDef> getClassificationWithName(String name) {
        AtlasClassificationDef classificationTypeDef =
                AtlasTypeUtil.createTraitTypeDef(name, "s_description", Collections.<String>emptySet(),
                        AtlasTypeUtil.createRequiredAttrDef("level", "int"));


        List<AtlasClassificationDef> ret = Arrays.asList(classificationTypeDef);

        populateSystemAttributes(ret);

        return ret;
    }

    public static AtlasClassificationDef getSingleClassificationWithName(String name) {
        AtlasClassificationDef classificaitonTypeDef =
                AtlasTypeUtil.createTraitTypeDef(name, "s_description", Collections.<String>emptySet(),
                        AtlasTypeUtil.createRequiredAttrDef("level", "int"));

        populateSystemAttributes(classificaitonTypeDef);

        return classificaitonTypeDef;
    }

    public static List<AtlasClassificationDef> getClassificationWithValidAttribute(){
        return getClassificationWithValidSuperType();
    }

    public static List<AtlasEntityDef> getEntityWithValidSuperType() {
        AtlasEntityDef developerTypeDef = AtlasTypeUtil.createClassTypeDef("Developer", "Developer_description", Collections.singleton("Employee"),
                new AtlasAttributeDef("language", String.format("array<%s>", "string"), false, AtlasAttributeDef.Cardinality.SET,
                        1, 10, false, false,
                        Collections.<AtlasConstraintDef>emptyList()));

        List<AtlasEntityDef> ret = Arrays.asList(developerTypeDef);

        populateSystemAttributes(ret);

        return ret;
    }

    public static List<AtlasEntityDef> getEntityWithName(String name) {
        AtlasEntityDef developerTypeDef = AtlasTypeUtil.createClassTypeDef(name, "Developer_description", Collections.<String>emptySet(),
                new AtlasAttributeDef("language", String.format("array<%s>", "string"), false, AtlasAttributeDef.Cardinality.SET,
                        1, 10, false, false,
                        Collections.<AtlasConstraintDef>emptyList()));

        List<AtlasEntityDef> ret = Arrays.asList(developerTypeDef);

        populateSystemAttributes(ret);

        return ret;
    }

    public static AtlasEntityDef getSingleEntityWithName(String name) {
        AtlasEntityDef developerTypeDef = AtlasTypeUtil.createClassTypeDef(name, "Developer_description", Collections.<String>emptySet(),
                new AtlasAttributeDef("language", String.format("array<%s>", "string"), false, AtlasAttributeDef.Cardinality.SET,
                        1, 10, false, false,
                        Collections.<AtlasConstraintDef>emptyList()));

        return developerTypeDef;
    }

    public static List<AtlasEntityDef> getEntityWithValidAttribute() {
        List<AtlasEntityDef> entityDefs = getEntityWithValidSuperType();
        entityDefs.get(1).getSuperTypes().clear();
        return entityDefs;
    }

    public static AtlasClassificationDef getClassificationWithInvalidSuperType() {
        AtlasClassificationDef classificationDef = simpleType().getClassificationDefs().get(0);
        classificationDef.getSuperTypes().add("!@#$%");
        return classificationDef;
    }

    public static AtlasEntityDef getEntityWithInvalidSuperType() {
        AtlasEntityDef entityDef = simpleType().getEntityDefs().get(0);
        entityDef.addSuperType("!@#$%");
        return entityDef;
    }

    public static void populateSystemAttributes(AtlasTypesDef typesDef) {
        populateSystemAttributes(typesDef.getEnumDefs());
        populateSystemAttributes(typesDef.getStructDefs());
        populateSystemAttributes(typesDef.getClassificationDefs());
        populateSystemAttributes(typesDef.getEntityDefs());
        populateSystemAttributes(typesDef.getRelationshipDefs());
    }

    public static void populateSystemAttributes(List<? extends AtlasBaseTypeDef> typeDefs) {
        if (CollectionUtils.isNotEmpty(typeDefs)) {
            for (AtlasBaseTypeDef typeDef : typeDefs) {
                populateSystemAttributes(typeDef);
            }
        }
    }

    public static void populateSystemAttributes(AtlasBaseTypeDef typeDef) {
        typeDef.setCreatedBy(TestUtilsV2.TEST_USER);
        typeDef.setUpdatedBy(TestUtilsV2.TEST_USER);
    }
}
