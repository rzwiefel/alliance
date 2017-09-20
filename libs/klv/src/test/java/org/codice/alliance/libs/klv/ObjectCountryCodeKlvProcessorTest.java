/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.libs.klv;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import ddf.catalog.data.Attribute;
import java.util.Arrays;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ObjectCountryCodeKlvProcessorTest {
  @Test
  public void test() {

    String id1 = "ID1";
    String id2 = "ID2";

    ArgumentCaptor<Attribute> argumentCaptor =
        KlvUtilities.testKlvProcessor(
            new ObjectCountryCodesKlvProcessor(),
            Stanag4609TransportStreamParser.OBJECT_COUNTRY_CODES,
            Arrays.asList(id1, id2, id1, id2));

    assertThat(
        argumentCaptor.getValue().getName(), is(AttributeNameConstants.OBJECT_COUNTRY_CODES));
    assertThat(argumentCaptor.getValue().getValues(), hasSize(2));
    assertThat(
        argumentCaptor.getValue().getValues().containsAll(Arrays.asList(id1, id2)), is(true));
  }
}
