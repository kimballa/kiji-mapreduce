/**
 * (c) Copyright 2012 WibiData, Inc.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kiji.mapreduce.context;

import java.io.IOException;

import com.google.common.base.Preconditions;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

import org.kiji.mapreduce.InternalProducerContextInterface;
import org.kiji.schema.EntityId;
import org.kiji.schema.KijiColumnName;
import org.kiji.schema.KijiPutter;
import org.kiji.schema.KijiRowData;

/** Implementation of a producer context. */
public class InternalProducerContext
    extends InternalKijiContext
    implements InternalProducerContextInterface {

  /** Interface to write to the output table. */
  private final KijiPutter mPutter;

  /** Family to write to. */
  private final String mFamily;

  /** Qualifier to write to (may be null, when output is a map-type family). */
  private final String mQualifier;

  /** The entity id of the row being written. */
  private EntityId mEntityId;

  /**
   * Initializes a producer context.
   *
   * @param context Underlying Hadoop context.
   * @param putter Putter to write to the table.
   * @param outputColumn Column to write.
   * @throws IOException on I/O error.
   */
  public InternalProducerContext(
      TaskInputOutputContext<EntityId, KijiRowData, ?, ?> context,
      KijiPutter putter,
      KijiColumnName outputColumn)
      throws IOException {
    super(context);
    mPutter = Preconditions.checkNotNull(putter);
    mFamily = Preconditions.checkNotNull(outputColumn.getFamily());
    mQualifier = outputColumn.getQualifier();
  }

  /** {@inheritDoc} */
  @Override
  public EntityId getEntityId() {
    return mEntityId;
  }

  /** {@inheritDoc} */
  @Override
  public InternalProducerContext setEntityId(EntityId entityId) {
    mEntityId = entityId;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public <T> void put(T value)
      throws IOException {
    put(HConstants.LATEST_TIMESTAMP, value);
  }

  /** {@inheritDoc} */
  @Override
  public <T> void put(String qualifier, T value)
      throws IOException {
    put(qualifier, HConstants.LATEST_TIMESTAMP, value);
  }

  /** {@inheritDoc} */
  @Override
  public <T> void put(long timestamp, T value)
      throws IOException {
    Preconditions.checkNotNull(mEntityId);
    Preconditions.checkNotNull(mQualifier,
        "Producer output configured for a map-type family, use put(qualifier, timestamp, value)");
    mPutter.put(mEntityId, mFamily, mQualifier, timestamp, value);
  }

  /** {@inheritDoc} */
  @Override
  public <T> void put(String qualifier, long timestamp, T value)
      throws IOException {
    Preconditions.checkNotNull(mEntityId);
    Preconditions.checkState(null == mQualifier,
        "Qualifier already specified by producer configuration.");

    mPutter.put(mEntityId, mFamily, qualifier, timestamp, value);
  }
}
