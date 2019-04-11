/*
 * Copyright 2014-2019 Netflix, Inc.
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
package com.netflix.spectator.prometheus;

import com.netflix.spectator.api.AbstractRegistry;
import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DistributionSummary;
import com.netflix.spectator.api.Gauge;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Tag;
import com.netflix.spectator.api.Timer;
import io.prometheus.client.SimpleCollector;

import java.util.ArrayList;
import java.util.List;

/** Registry implementation that maps spectator types to the prometheus simple client library. */
public class PrometheusRegistry extends AbstractRegistry {

  private final io.prometheus.client.CollectorRegistry impl;

  /** Create a new instance. */
  public PrometheusRegistry() {
    this(Clock.SYSTEM, io.prometheus.client.CollectorRegistry.defaultRegistry);
  }

  /** Create a new instance. */
  public PrometheusRegistry(Clock clock, io.prometheus.client.CollectorRegistry impl) {
    super(clock);
    this.impl = impl;
  }

  private String tagToLabel(Tag tag){
    return tag.key() + "_" + tag.value();
  }

  private String[] tagsToLabels(Iterable<Tag> tags){
    List<String> l = new ArrayList<>();
    tags.forEach((t) -> l.add(tagToLabel(t)));
    return l.toArray(new String[l.size()]);
  }

  private <B extends SimpleCollector.Builder<B, C>, C extends SimpleCollector> C build(B builder, Id id){
    return builder.name(id.name()).help(id.name()).labelNames(tagsToLabels(id.tags())).register(impl);
  }

  @Override protected Counter newCounter(Id id) {
    return new PrometheusCounter(clock(), id, build(io.prometheus.client.Counter.build(), id));
  }

  @Override protected DistributionSummary newDistributionSummary(Id id) {
    return new PrometheusDistributionSummary(clock(), id, build(io.prometheus.client.Summary.build(), id));
  }

  @Override protected Timer newTimer(Id id) {
    return new PrometheusTimer(clock(), id, build(io.prometheus.client.Summary.build(), id));
  }

  @Override protected Gauge newGauge(Id id) {
    return new PrometheusGauge(clock(), id, build(io.prometheus.client.Gauge.build(), id));
  }

  @Override protected Gauge newMaxGauge(Id id) {
    return new PrometheusGauge(clock(), id, build(io.prometheus.client.Gauge.build(), id)); // FIXME this is incorrect behaviour. Unfortunatelly it's hard to implement a proper MaxGauge with prometheus.
  }
}
