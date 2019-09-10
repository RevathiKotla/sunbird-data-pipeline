/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ekstep.ep.samza.domain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DeviceProfile {
  private String countryCode;
  private String country;
  private String stateCode;
  private String state;
  private String city;
  private String district;
  private String districtCustom;
  private String stateCodeCustom;
  private String stateCustomName;
  private Map<String, String> uaspec;
  private Map<String, String> devicespec;
  private Long firstaccess;
  private Gson gson = new Gson();
  private Type type = new TypeToken<Map<String, Object>>() {}.getType();

  public DeviceProfile() {
    this.countryCode = "";
    this.country = "";
    this.stateCode = "";
    this.state = "";
    this.city = "";
    this.district = "";
    this.districtCustom = "";
    this.stateCodeCustom = "";
    this.stateCustomName = "";
    this.uaspec = new HashMap<>();
    this.devicespec = new HashMap<>();
    this.firstaccess = 0L;
  }

  public Map<String, String> toMap() {
    Map<String, String> values = new HashMap<>();
    values.put("country_code", DeviceProfile.getValueOrDefault(this.countryCode, ""));
    values.put("country", DeviceProfile.getValueOrDefault(this.country, ""));
    values.put("state_code", DeviceProfile.getValueOrDefault(this.stateCode, ""));
    values.put("state", DeviceProfile.getValueOrDefault(this.state, ""));
    values.put("city", DeviceProfile.getValueOrDefault(this.city, ""));
    values.put("district_custom", DeviceProfile.getValueOrDefault(this.districtCustom, ""));
    values.put("state_custom", DeviceProfile.getValueOrDefault(this.stateCustomName, ""));
    values.put("state_code_custom", DeviceProfile.getValueOrDefault(this.stateCodeCustom, ""));
    values.put("uaspec", gson.toJson(DeviceProfile.getValueOrDefault(this.uaspec, new HashMap<>())));
    values.put("devicespec", gson.toJson(DeviceProfile.getValueOrDefault(this.devicespec, new HashMap<>())));
    values.put("firstaccess", DeviceProfile.getValueOrDefault(String.valueOf(this.firstaccess), ""));
    return values;
  }

  public DeviceProfile fromMap(Map<String, String> map) {
    this.countryCode = map.getOrDefault("country_code", "");
    this.country = map.getOrDefault("country", "");
    this.stateCode = map.getOrDefault("state_code", "");
    this.state = map.getOrDefault("state", "");
    this.city = map.getOrDefault("city", "");
    this.districtCustom = map.getOrDefault("district_custom", "");
    this.stateCustomName = map.getOrDefault("state_custom", "");
    this.stateCodeCustom = map.getOrDefault("state_code_custom", "");
    this.uaspec = gson.fromJson(map.getOrDefault("uaspec", ""), type);
    this.devicespec = gson.fromJson(map.getOrDefault("device_spec", ""), type);
    this.firstaccess = Long.valueOf(map.getOrDefault("firstaccess", "0"));
    return this;
  }

  public static <T> T getValueOrDefault(T value, T defaultValue) {
    return value == null ? defaultValue : value;
  }
}
