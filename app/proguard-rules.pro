# Our own code is static data plus Compose and uses no reflection, so it needs no rules.
# The ads dependency chain does.
#
# play-services-ads pulls in WorkManager transitively. WorkManager keeps its queue in a
# Room database, and Room looks up the *generated* WorkDatabase_Impl class by name at
# runtime. R8 runs in full mode by default under AGP 8+, cannot see a reflective
# reference, strips the class, and the app then dies inside
# androidx.startup.InitializationProvider before any of our code runs:
#
#   Unable to get provider androidx.startup.InitializationProvider
#   Caused by: Failed to create an instance of androidx.work.impl.WorkDatabase
#
# This only ever shows up in a minified build, so debug runs will not catch a regression
# here — test the release variant before shipping.

# Room resolves "<DatabaseClass>_Impl" by name and needs its no-arg constructor.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# androidx.startup instantiates initializers listed in the merged manifest by class name.
-keep class * extends androidx.startup.Initializer { <init>(); }
