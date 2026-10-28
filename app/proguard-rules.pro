# kotlinx.serialization: keep generated serializers for @Serializable classes.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers @kotlinx.serialization.Serializable class net.rubinny.willowbrook.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class net.rubinny.willowbrook.** {
    kotlinx.serialization.KSerializer serializer(...);
}
