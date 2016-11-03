/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.adapter.geode.rel;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;

/**
 * Implementation of {@link Sort}
 * relational expression in Geode.
 */
public class GeodeAggregationSort extends Sort implements GeodeRel {

  public GeodeAggregationSort(RelOptCluster cluster, RelTraitSet traitSet,
                              RelNode child, RelCollation collation,
                              RexNode fetch) {
    super(cluster, traitSet, child, collation, null, fetch);

    assert getConvention() == GeodeRel.CONVENTION;
    assert getConvention() == child.getConvention();
  }

  @Override public RelOptCost computeSelfCost(RelOptPlanner planner,
                                              RelMetadataQuery mq) {

    RelOptCost cost = super.computeSelfCost(planner, mq);
    if (fetch != null) {
      // We do this so we get the limit for free
      return planner.getCostFactory().makeZeroCost();
    }

    return super.computeSelfCost(planner, mq).multiplyBy(0.1);
  }

  @Override public Sort copy(RelTraitSet traitSet, RelNode input,
                             RelCollation newCollation, RexNode offset, RexNode fetch) {
    return new GeodeAggregationSort(getCluster(), traitSet, input, collation,
        fetch);
  }

  public void implement(Implementor implementor) {
    implementor.visitChild(0, getInput());

    if (fetch != null) {
      implementor.setLimit(((RexLiteral) fetch).getValue().toString());
    }
  }
}
// End GeodeAggregationSort.java
