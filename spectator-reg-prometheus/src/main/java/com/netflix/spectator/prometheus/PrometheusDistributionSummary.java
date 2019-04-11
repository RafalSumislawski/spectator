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

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.DistributionSummary;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import io.prometheus.client.Summary;

import java.util.Arrays;

/** Distribution summary implementation for the prometheus registry. */
class PrometheusDistributionSummary implements DistributionSummary {

  private final Clock clock;
  private final Id id;
  private final io.prometheus.client.Summary impl;

  /** Create a new instance. */
  PrometheusDistributionSummary(Clock clock, Id id, io.prometheus.client.Summary impl) {
    this.clock = clock;
    this.id = id;
    this.impl = impl;
  }

  @Override public Id id() {
    return id;
  }

  @Override public boolean hasExpired() {
    return false;
  }

  @Override public void record(long amount) {
    impl.observe(amount);
  }

  @Override public Iterable<Measurement> measure() {
    final long now = clock.wallTime();
    Summary.Child.Value value = impl.get();
    return Arrays.asList(
            new Measurement(id, now, value.count),
            new Measurement(id, now, value.sum));
  }

  @Override public long count() {
    return (long) impl.get().count;
  }

  @Override public long totalAmount() {
    return (long) impl.get().sum;
  }
}
