/*
 * Copyright 2022 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.hive.bigquery.connector.input.udfs;

/** Converts Hive's datediff() function to BigQuery's date_diff() function. */
public class BigQueryUDFDateDiff extends BigQueryUDFBase {

  public BigQueryUDFDateDiff() {}

  @Override
  public String getDisplayString(String[] children) {
    return String.format("DATE_DIFF(%s, %s, DAY)", children[0], children[1]);
  }
}
