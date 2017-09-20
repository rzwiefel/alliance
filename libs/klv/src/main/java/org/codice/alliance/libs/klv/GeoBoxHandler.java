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

import ddf.catalog.data.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.data.numerical.KlvIntegerEncodedFloatingPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler expects four latitude-longitude pairs. It generates a WKT polygon for each four-pair
 * set.
 */
public class GeoBoxHandler extends BaseKlvHandler implements Trimmable {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoBoxHandler.class);

  private String latitude1;

  private String longitude1;

  private String latitude2;

  private String longitude2;

  private String latitude3;

  private String longitude3;

  private String latitude4;

  private String longitude4;

  private Map<String, List<Double>> map = new HashMap<>();

  /**
   * @param attributeName the name of the metacard attribute being generated
   * @param latitude1 the name of the stanag 4609 field
   * @param longitude1 the name of the stanag 4609 field
   * @param latitude2 the name of the stanag 4609 field
   * @param longitude2 the name of the stanag 4609 field
   * @param latitude3 the name of the stanag 4609 field
   * @param longitude3 the name of the stanag 4609 field
   * @param latitude4 the name of the stanag 4609 field
   * @param longitude4 the name of the stanag 4609 field
   */
  public GeoBoxHandler(
      String attributeName,
      String latitude1,
      String longitude1,
      String latitude2,
      String longitude2,
      String latitude3,
      String longitude3,
      String latitude4,
      String longitude4) {
    super(attributeName);

    this.latitude1 = latitude1;
    this.longitude1 = longitude1;

    this.latitude2 = latitude2;
    this.longitude2 = longitude2;

    this.latitude3 = latitude3;
    this.longitude3 = longitude3;

    this.latitude4 = latitude4;
    this.longitude4 = longitude4;
  }

  public String getLatitude1() {
    return latitude1;
  }

  public String getLatitude2() {
    return latitude2;
  }

  public String getLatitude3() {
    return latitude3;
  }

  public String getLatitude4() {
    return latitude4;
  }

  public String getLongitude1() {
    return longitude1;
  }

  public String getLongitude2() {
    return longitude2;
  }

  public String getLongitude3() {
    return longitude3;
  }

  public String getLongitude4() {
    return longitude4;
  }

  public Map<String, List<Double>> getRawGeoData() {
    return map;
  }

  @Override
  public Optional<Attribute> asAttribute() {

    int minimumListSize = getMinimumListSize();

    List<String> polygonsWkts = new ArrayList<>();

    for (int i = 0; i < minimumListSize; i++) {
      polygonsWkts.add(
          String.format(
              "POLYGON ((%f %f, %f %f, %f %f, %f %f, %f %f))",
              map.get(longitude1).get(i),
              map.get(latitude1).get(i),
              map.get(longitude2).get(i),
              map.get(latitude2).get(i),
              map.get(longitude3).get(i),
              map.get(latitude3).get(i),
              map.get(longitude4).get(i),
              map.get(latitude4).get(i),
              map.get(longitude1).get(i),
              map.get(latitude1).get(i)));
    }

    return asAttribute(polygonsWkts);
  }

  public GeoBoxHandler asSubsampledHandler(int subsampleCount) {

    if (getRawGeoData().isEmpty()) {
      return this;
    }

    int size = getRawGeoData().get(getLatitude1()).size();

    if (size <= subsampleCount) {
      return this;
    }

    GeoBoxHandler out =
        new GeoBoxHandler(
            getAttributeName(),
            getLatitude1(),
            getLongitude1(),
            getLatitude2(),
            getLongitude2(),
            getLatitude3(),
            getLongitude3(),
            getLatitude4(),
            getLongitude4());

    subsample(getRawGeoData(), subsampleCount, size, out::accept);

    return out;
  }

  /** Trim the arrays of lat and lon values to the same length. */
  @Override
  public void trim() {

    int minListSize = getMinimumListSize();

    map.keySet()
        .forEach(
            key ->
                map.computeIfPresent(
                    key,
                    (fieldName, list) ->
                        list.size() > minListSize ? list.subList(0, minListSize) : list));
  }

  @Override
  public void accept(KlvDataElement klvDataElement) {
    if (!(klvDataElement instanceof KlvIntegerEncodedFloatingPoint)) {
      LOGGER.debug(
          "non-KlvIntegerEncodedFloatingPoint data was passed to the GeoBoxHandler: name = {} klvDataElement = {}",
          klvDataElement.getName(),
          klvDataElement);
      return;
    }

    accept(klvDataElement.getName(), ((KlvIntegerEncodedFloatingPoint) klvDataElement).getValue());
  }

  @Override
  public void reset() {
    map.clear();
  }

  public void accept(String name, Double value) {
    map.putIfAbsent(name, new ArrayList<>());
    map.get(name).add(value);
  }

  private int getMinimumListSize() {
    return getMinimumListSize(map.values());
  }
}
