// /*
//  * SPDX-License-Identifier: Apache-2.0
//  */
// package org.hyperledger.fabric.samples.willtransfer;

// import java.util.Objects;
// import java.util.List;

// import org.hyperledger.fabric.contract.annotation.DataType;
// import org.hyperledger.fabric.contract.annotation.Property;

// import com.owlike.genson.annotation.JsonProperty;

// @DataType()
// public final class Will {

//     @Property()
//     private final String willId;

//     @Property()
//     private final String owner;

//     @Property()
//     private final List<String> beneficiaries;

//     @Property()
//     private final String content;

//     @Property()
//     private final String timestamp;

//     public String getWillId() {
//         return willId;
//     }

//     public String getOwner() {
//         return owner;
//     }

//     public List<String> getBeneficiaries() {
//         return beneficiaries;
//     }

//     public String getContent() {
//         return content;
//     }

//     public String getTimestamp() {
//         return timestamp;
//     }

//     public Will(@JsonProperty("willId") final String willId, @JsonProperty("owner") final String owner,
//                 @JsonProperty("beneficiaries") final List<String> beneficiaries, @JsonProperty("content") final String content,
//                 @JsonProperty("timestamp") final String timestamp) {
//         this.willId = willId;
//         this.owner = owner;
//         this.beneficiaries = beneficiaries;
//         this.content = content;
//         this.timestamp = timestamp;
//     }

//     @Override
//     public int hashCode() {
//         return Objects.hash(getWillId(), getOwner(), getBeneficiaries(), getContent(), getTimestamp());
//     }

//     @Override
//     public boolean equals(final Object obj) {
//         if (this == obj) {
//             return true;
//         }
//         if (obj == null || getClass() != obj.getClass()) {
//             return false;
//         }

//         Will will = (Will) obj;

//         return willId.equals(will.willId)
//                 &&
//                 owner.equals(will.owner)
//                 &&
//                 beneficiaries.equals(will.beneficiaries)
//                 &&
//                 content.equals(will.content)
//                 &&
//                 timestamp.equals(will.timestamp);
//     }

//     @Override
//     public String toString() {
//         return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [willId=" + willId + ", owner="
//                 + owner + ", content=" + content + ", timestamp=" + timestamp + "]";
//     }
// }
