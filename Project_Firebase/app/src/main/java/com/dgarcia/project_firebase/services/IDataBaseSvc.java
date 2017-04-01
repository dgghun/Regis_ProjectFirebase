package com.dgarcia.project_firebase.services;


import com.dgarcia.project_firebase.model.TestObject;

import java.util.List;

public interface IDataBaseSvc {

    public TestObject create(TestObject TestObject);
    public List<TestObject> retrieveAllTestObjects();
    public TestObject update(TestObject testObject);
    public TestObject delete(TestObject testObject);

}
