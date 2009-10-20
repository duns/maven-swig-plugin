package it0002.test;

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
import org.apache.maven.its.swig.Circle;
import org.apache.maven.its.swig.Square;
import org.apache.maven.its.swig.Shape;
import org.apache.maven.its.swig.NarSystem;

public class ClassTest {

// This example illustrates how C++ classes can be used from Java using SWIG.
// The Java class gets mapped onto the C++ class and behaves as if it is a Java class.

  @Test
  public void testClass() 
  {
    NarSystem.loadLibrary();

    // ----- Object creation -----
    Circle c = new Circle(10);
    Square s = new Square(10);
    
    // ----- Access a static member -----    
    Assert.assertEquals(2, Shape.getNshapes());
    
    // ----- Member data access -----    
    c.setX(20);
    c.setY(30);
    
    Shape shape = s;
    shape.setX(-10);
    shape.setY(5);

    Assert.assertEquals(20.0, c.getX(), 0.0);    
    Assert.assertEquals(30.0, c.getY(), 0.0);    
    Assert.assertEquals(-10.0, s.getX(), 0.0);    
    Assert.assertEquals(5.0, s.getY(), 0.0);    
    
    // ----- Call some methods -----
    Shape[] shapes = {c,s};
    Assert.assertEquals(314.1592653589793, shapes[0].area(), 0.0001);    
    Assert.assertEquals(62.83185307179586, shapes[0].perimeter(), 0.0001);    
    Assert.assertEquals(100.0, shapes[1].area(), 0.0001);    
    Assert.assertEquals(40.0, shapes[1].perimeter(), 0.0001);    
    
    c.delete();
    s.delete();

    Assert.assertEquals(0, Shape.getNshapes());
  }
}
