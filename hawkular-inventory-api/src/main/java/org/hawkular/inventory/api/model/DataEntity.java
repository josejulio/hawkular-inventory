/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.inventory.api.model;

import java.util.Map;

/**
 * A data entity is an entity wrapping the data. It's sole purpose is to give a path to the piece of structured data.
 *
 * <p>A data entity has an owner - the data entities are not shared amongst entities. Further, it has a role - a purpose
 * for which the owner holds on to the data.
 *
 * @author Lukas Krejci
 * @since 0.3.0
 */
public class DataEntity extends Entity<DataEntity.Blueprint, DataEntity.Update> {

    private final StructuredData value;

    private DataEntity() {
        this.value = null;
    }

    public DataEntity(CanonicalPath owner, Role role, StructuredData value) {
        super(owner.extend(DataEntity.class, role.name()).get());
        this.value = value;
    }

    public DataEntity(CanonicalPath owner, Role role, StructuredData value, Map<String, Object> properties) {
        super(owner.extend(DataEntity.class, role.name()).get(), properties);
        this.value = value;
    }

    public DataEntity(CanonicalPath path, StructuredData value, Map<String, Object> properties) {
        this(path.up(), Role.valueOf(path.getSegment().getElementId()), value, properties);
    }

    public StructuredData getValue() {
        return value;
    }

    public Role getRole() {
        return Role.valueOf(getId());
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> visitor, P parameter) {
        return visitor.visitData(this, null);
    }

    @Override
    public Updater<Update, ? extends AbstractElement<?, Update>> update() {
        return new Updater<>((u) -> new DataEntity(this.getPath().up(), this.getRole(),
                valueOrDefault(u.getValue(), getValue()), u.getProperties()));
    }

    public enum Role {
        configuration, connectionConfiguration;
    }

    public static final class Blueprint extends AbstractElement.Blueprint {
        private final StructuredData value;
        private final DataEntity.Role role;

        public static Builder builder() {
            return new Builder();
        }

        public Blueprint(DataEntity.Role role, StructuredData value, Map<String, Object> properties) {
            super(properties);
            this.role = role;
            this.value = value;
        }

        public StructuredData getValue() {
            return value;
        }

        public Role getRole() {
            return role;
        }

        @Override
        public <R, P> R accept(ElementBlueprintVisitor<R, P> visitor, P parameter) {
            return visitor.visitData(this, parameter);
        }

        public static final class Builder extends Entity.Blueprint.Builder<Blueprint, Builder> {

            private DataEntity.Role role;
            private StructuredData value;

            public Builder withValue(StructuredData value) {
                this.value = value;
                return this;
            }

            public Builder withRole(DataEntity.Role role) {
                this.role = role;
                return this;
            }

            @Override
            public Blueprint build() {
                if (role == null) {
                    throw new NullPointerException("Data entity role not specified.");
                }
                return new Blueprint(role, value, properties);
            }
        }
    }

    public static final class Update extends Entity.Update {
        private final StructuredData value;

        public static Builder builder() {
            return new Builder();
        }

        public Update(StructuredData value, Map<String, Object> properties) {
            super(properties);
            this.value = value;
        }

        public StructuredData getValue() {
            return value;
        }

        @Override
        public <R, P> R accept(ElementUpdateVisitor<R, P> visitor, P parameter) {
            return visitor.visitData(this, parameter);
        }

        public static final class Builder extends Entity.Update.Builder<Update, Builder> {

            private StructuredData value;

            public Builder withValue(StructuredData value) {
                this.value = value;
                return this;
            }

            @Override
            public Update build() {
                return new Update(value, properties);
            }
        }
    }
}
