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
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

import java.util.Collections;

class PrometheusGauge implements com.netflix.spectator.api.Gauge {

  private final Clock clock;
  private final Id id;
  private final io.prometheus.client.Gauge.Child impl;

  PrometheusGauge(Clock clock, Id id, io.prometheus.client.Gauge.Child impl) {
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

  @Override public Iterable<Measurement> measure() {
    return Collections.singleton(new Measurement(id, clock.wallTime(), value()));
  }

  @Override public void set(double v) {
    impl.set(v);
  }

  @Override public double value() {
    return impl.get();
  }
}
