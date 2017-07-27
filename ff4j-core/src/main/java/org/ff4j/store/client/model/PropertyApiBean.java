/*
 * FF4J (ff4j.org) WebAPI
 * Administrate and operate all tasks on your features through this api.
 *
 * OpenAPI spec version: 1.5
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package org.ff4j.store.client.model;

/*
 * #%L
 * ff4j-core
 * %%
 * Copyright (C) 2013 - 2017 FF4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PropertyApiBean
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2017-07-27T20:07:45.848Z")
public class PropertyApiBean {
  @SerializedName("description")
  private String description = null;

  @SerializedName("fixedValues")
  private List<String> fixedValues = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("type")
  private String type = null;

  @SerializedName("value")
  private String value = null;

  public PropertyApiBean description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public PropertyApiBean fixedValues(List<String> fixedValues) {
    this.fixedValues = fixedValues;
    return this;
  }

  public PropertyApiBean addFixedValuesItem(String fixedValuesItem) {
    if (this.fixedValues == null) {
      this.fixedValues = new ArrayList<String>();
    }
    this.fixedValues.add(fixedValuesItem);
    return this;
  }

   /**
   * Get fixedValues
   * @return fixedValues
  **/
  @ApiModelProperty(value = "")
  public List<String> getFixedValues() {
    return fixedValues;
  }

  public void setFixedValues(List<String> fixedValues) {
    this.fixedValues = fixedValues;
  }

  public PropertyApiBean name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PropertyApiBean type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public PropertyApiBean value(String value) {
    this.value = value;
    return this;
  }

   /**
   * Get value
   * @return value
  **/
  @ApiModelProperty(value = "")
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PropertyApiBean propertyApiBean = (PropertyApiBean) o;
    return Objects.equals(this.description, propertyApiBean.description) &&
        Objects.equals(this.fixedValues, propertyApiBean.fixedValues) &&
        Objects.equals(this.name, propertyApiBean.name) &&
        Objects.equals(this.type, propertyApiBean.type) &&
        Objects.equals(this.value, propertyApiBean.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, fixedValues, name, type, value);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PropertyApiBean {\n");
    
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    fixedValues: ").append(toIndentedString(fixedValues)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
  
}

