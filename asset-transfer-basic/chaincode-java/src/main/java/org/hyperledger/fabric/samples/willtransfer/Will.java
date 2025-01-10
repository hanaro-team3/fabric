package org.hyperledger.fabric.samples.willtransfer;

import java.util.List;
import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Will {

    @Property()
    private final String willId;

    @Property()
    private final List<Inheritance> inheritances;

    @Property()
    private final List<Executor> executors;

    @Property()
    private final List<WillContent> wills;

    @Property()
    private final int shareAt;

    public Will(@JsonProperty("willId") final String willId,
                @JsonProperty("inheritances") final List<Inheritance> inheritances,
                @JsonProperty("executors") final List<Executor> executors,
                @JsonProperty("wills") final List<WillContent> wills,
                @JsonProperty("shareAt") final int shareAt) {
        this.willId = willId;
        this.inheritances = inheritances;
        this.executors = executors;
        this.wills = wills;
        this.shareAt = shareAt;
    }

    public String getWillId() {
        return willId;
    }

    public List<Inheritance> getInheritances() {
        return inheritances;
    }

    public List<Executor> getExecutors() {
        return executors;
    }

    public List<WillContent> getWills() {
        return wills;
    }

    public int getShareAt() {
        return shareAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(willId, inheritances, executors, wills, shareAt);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Will other = (Will) obj;
        return willId.equals(other.willId)
                &&
               inheritances.equals(other.inheritances)
                &&
               executors.equals(other.executors)
                &&
               wills.equals(other.wills)
                &&
               shareAt == other.shareAt;
    }

    @Override
    public String toString() {
        return String.format("Will [willId=%s, inheritances=%s, executors=%s, wills=%s, shareAt=%d]",
                             willId, inheritances, executors, wills, shareAt);
    }

    @DataType()
    public static final class Inheritance {
        @Property()
        private final String name;
        @Property()
        private final String relation;
        @Property()
        private final String asset;

        public Inheritance(@JsonProperty("name") final String name,
                           @JsonProperty("relation") final String relation,
                           @JsonProperty("asset") final String asset) {
            this.name = name;
            this.relation = relation;
            this.asset = asset;
        }

        public String getName() {
            return name;
        }

        public String getRelation() {
            return relation;
        }

        public String getAsset() {
            return asset;
        }
    }

    @DataType()
    public static final class Executor {
        @Property()
        private final String name;
        @Property()
        private final String relation;

        public Executor(@JsonProperty("name") final String name,
                        @JsonProperty("relation") final String relation) {
            this.name = name;
            this.relation = relation;
        }

        public String getName() {
            return name;
        }

        public String getRelation() {
            return relation;
        }
    }

    @DataType()
    public static final class WillContent {
        @Property()
        private final String relation;
        @Property()
        private final String content;

        public WillContent(@JsonProperty("relation") final String relation,
                           @JsonProperty("content") final String content) {
            this.relation = relation;
            this.content = content;
        }

        public String getRelation() {
            return relation;
        }

        public String getContent() {
            return content;
        }
    }
}
