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
package com.google.cloud.hive.bigquery.connector.output;

import com.google.cloud.hive.bigquery.connector.JobDetails;
import com.google.cloud.hive.bigquery.connector.config.HiveBigQueryConfig;
import com.google.cloud.hive.bigquery.connector.output.direct.DirectOutputCommitter;
import com.google.cloud.hive.bigquery.connector.output.indirect.IndirectOutputCommitter;
import com.google.cloud.hive.bigquery.connector.utils.FileSystemUtils;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobContext;
import org.apache.hadoop.mapred.OutputCommitter;
import org.apache.hadoop.mapred.TaskAttemptContext;

public class BigQueryOutputCommitter extends OutputCommitter {

  public static void commit(Configuration conf, JobDetails jobDetails) throws IOException {
    String writeMethod =
        conf.get(HiveBigQueryConfig.WRITE_METHOD_KEY, HiveBigQueryConfig.WRITE_METHOD_DIRECT);
    // Pick the appropriate OutputCommitter (direct or indirect) based on the
    // configured write method
    if (HiveBigQueryConfig.WRITE_METHOD_INDIRECT.equals(writeMethod)) {
      IndirectOutputCommitter.commitJob(conf, jobDetails);
    } else if (HiveBigQueryConfig.WRITE_METHOD_DIRECT.equals(writeMethod)) {
      DirectOutputCommitter.commitJob(conf, jobDetails);
    } else {
      throw new RuntimeException("Invalid write method setting: " + writeMethod);
    }
    FileSystemUtils.deleteWorkDirOnExit(conf, jobDetails.getHmsDbTableName());
  }

  @Override
  public void commitJob(JobContext jobContext) throws IOException {
    JobConf conf = jobContext.getJobConf();
    String hmsDbTableName = conf.get("name");
    if (hmsDbTableName.isEmpty()) {
      throw new RuntimeException("JobConf does not have output table name");
    }
    JobDetails jobDetails = JobDetails.readJobDetailsFile(conf, hmsDbTableName);
    commit(conf, jobDetails);
    super.commitJob(jobContext);
  }

  /**
   * This method is called automatically at the end of a failed job when using the "mr" execution
   * engine.
   */
  @Override
  public void abortJob(JobContext jobContext, int status) throws IOException {
    JobConf conf = jobContext.getJobConf();
    String hmsDbTableName = conf.get("name");
    if (hmsDbTableName.isEmpty()) {
      throw new RuntimeException("jobContext does not have output table name");
    }
    JobDetails jobDetails = JobDetails.readJobDetailsFile(conf, hmsDbTableName);
    if (!jobDetails.getHmsDbTableName().equals(hmsDbTableName)) {
      throw new RuntimeException("hive table not matching in jobDetails and jobContext");
    }
    DirectOutputCommitter.abortJob(conf, jobDetails);
    FileSystemUtils.deleteWorkDirOnExit(jobContext.getJobConf(), jobDetails.getHmsDbTableName());
    super.abortJob(jobContext, status);
  }

  @Override
  public void setupJob(JobContext jobContext) throws IOException {
    // Do nothing
  }

  @Override
  public void setupTask(TaskAttemptContext taskAttemptContext) throws IOException {
    // Do nothing
  }

  @Override
  public boolean needsTaskCommit(TaskAttemptContext taskAttemptContext) throws IOException {
    return false;
  }

  @Override
  public void commitTask(TaskAttemptContext taskAttemptContext) throws IOException {
    // Do nothing
  }

  @Override
  public void abortTask(TaskAttemptContext taskAttemptContext) throws IOException {
    // Do nothing
  }
}
