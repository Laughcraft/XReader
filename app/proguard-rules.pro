-dontobfuscate
-keepattributes InnerClasses
-dontoptimize
-verbose

-printusage

-keep class org.apache.poi.** { *; }
-keepclassmembers class org.apache.poi.ss.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.apache.codehaus.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class org.w3c.dom.** { *; }
-keep class org.dom4j.** { *; }
-keep class javax.** { *; }

-keep class schemaorg_apache_xmlbeans.system.sF1327CCA741569E70F9CA8C9AF9B44B2.TypeSystemHolder { public final static *** typeSystem; }
-keep class schemaorg_apache_xmlbeans.** { *; }
-keep class schemasMicrosoftComOfficeExcel.** { *; }
-keep class schemasMicrosoftComOfficeOffice.** { *; }
-keep class schemasMicrosoftComOfficeWord.** { *; }
-keep class aavax.xml.** { *; }

-keep class repackage.** { *; }

-keep class com.fasterxml.aalto.stax.InputFactoryImpl
-keep class com.fasterxml.aalto.stax.OutputFactoryImpl
-keep class com.fasterxml.aalto.stax.EventFactoryImpl