package it0001.test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.junit.Assert;
import org.junit.Test;
import org.apache.maven.its.swig.Example;
import org.apache.maven.its.swig.NarSystem;

public class SimpleTest {

    @Test
    public void simpleTest() {
       NarSystem.loadLibrary();

    // Call our gcd() function
    
    int x = 42;
    int y = 105;
    int g = Example.gcd(x,y);
    Assert.assertEquals(21, g);
    
    // Manipulate the Foo global variable
    
    // Output its current value
    Assert.assertEquals(3.0, Example.getFoo(), 0.0001);
    
    // Change its value
    Example.setFoo(3.1415926);
    
    // See if the change took effect
    Assert.assertEquals(3.1415926, Example.getFoo(), 0.0001);
  }
}
