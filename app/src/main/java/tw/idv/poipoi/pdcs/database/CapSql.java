package tw.idv.poipoi.pdcs.database;

/**
 * Created by DuST on 2017/4/29.
 */

public class CapSql {

    //region CAP Table Name
    static class Alert {
        public static final String TABLE_NAME = "alert";

        public static final String CAP_ID = "identifier";
        public static final String SENDER = "sender";
        public static final String SEND_TIME = "sent";
        public static final String STATUS = "status";
        public static final String MSG_TYPE = "msgType";
        public static final String SOURCE = "source";
        public static final String SCOPE = "scope";
        public static final String RESTRICTION = "restriction";
        public static final String ADDRESSES = "addresses";
        public static final String NOTE = "note";
        public static final String REFERENCES = "references";
        public static final String INCIDENTS = "incidents";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        CAP_ID + " TEXT PRIMARY KEY, " +
                        SENDER + " TEXT NOT NULL, " +
                        SEND_TIME + " TEXT NOT NULL, " +
                        STATUS + " TEXT NOT NULL, " +
                        MSG_TYPE + " TEXT NOT NULL, " +
                        SOURCE + " TEXT, " +
                        SCOPE + " TEXT NOT NULL, " +
                        RESTRICTION + " TEXT, " +
                        ADDRESSES + " TEXT, " +
                        NOTE + " TEXT, " +
                        REFERENCES + " TEXT, " +
                        INCIDENTS + " TEXT)";

        static class HandlingCode {
            public static final String TABLE_NAME = "alert_code";

            public static final String CAP_ID = Alert.CAP_ID;
            public static final String CODE_ID = "codeId";
            public static final String CODE = "code";

            public static final String CREATE_TABLE =
                    "CREATE TABLE " + TABLE_NAME + " (" +
                            CAP_ID + " TEXT PRIMARY KEY, " +
                            CODE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            CODE + " TEXT NOT NULL)";
        }

        static class Info {
            public static final String TABLE_NAME = "alert_info";

            public static final String CAP_ID = Alert.CAP_ID;
            public static final String INFO_ID = "infoId";
            public static final String LANGUAGE = "language";
            public static final String EVENT = "event";
            public static final String URGENCY = "urgency";
            public static final String SEVERITY = "severity";
            public static final String CERTAINTY = "certainty";
            public static final String AUDIENCE = "audience";
            public static final String EFFECTIVE = "effective";
            public static final String ONSET = "onset";
            public static final String EXPIRES = "expires";
            public static final String SENDER_NAME = "senderName";
            public static final String HEADLINE = "headline";
            public static final String DESCRIPTION = "description";
            public static final String INSTRUCTION = "instruction";
            public static final String WEB = "web";
            public static final String CONTACT = "contact";

            public static final String CREATE_TABLE =
                    "CREATE TABLE " + TABLE_NAME + " (" +
                            CAP_ID + " TEXT PRIMARY KEY, " +
                            INFO_ID + " TEXT PRIMARY KEY, " +
                            LANGUAGE + " TEXT, " +
                            EVENT + " TEXT NOT NULL, " +
                            URGENCY + " TEXT NOT NULL, " +
                            SEVERITY + " TEXT NOT NULL, " +
                            CERTAINTY + " TEXT NOT NULL, " +
                            AUDIENCE + " TEXT, " +
                            EFFECTIVE + " TEXT, " +
                            ONSET + " TEXT, " +
                            EXPIRES + " TEXT, " +
                            SENDER_NAME + " TEXT, " +
                            HEADLINE + " TEXT, " +
                            DESCRIPTION + " TEXT, " +
                            INSTRUCTION + " TEXT, " +
                            WEB + " TEXT, " +
                            CONTACT + " TEXT)";

            static class Category{
                public static final String TABLE_NAME = "info_category";

                public static final String INFO_ID = Info.INFO_ID;
                public static final String CATEGORY_ID = "categoryId";
                public static final String CATEGORY = "category";

                public static final String CREATE_TABLE =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                                INFO_ID + " TEXT PRIMARY KEY, " +
                                CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                CATEGORY + " TEXT)";
            }

            static class Response_Type{
                public static final String TABLE_NAME = "info_responseType";

                public static final String INFO_ID = Info.INFO_ID;
                public static final String RESPONSE_TYPE_ID = "responseTypeId";
                public static final String RESPONSE_TYPE = "responseType";

                public static final String CREATE_TABLE =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                                INFO_ID + " TEXT PRIMARY KEY, " +
                                RESPONSE_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                RESPONSE_TYPE + " TEXT)";
            }

            static class Event_Code{
                public static final String TABLE_NAME = "info_responseType";

                public static final String INFO_ID = Info.INFO_ID;
                public static final String EVENT_CODE_ID = "eventCodeId";
                public static final String VALUE_NAME = "valueName";
                public static final String VALUE = "value";

                public static final String CREATE_TABLE =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                                INFO_ID + " TEXT PRIMARY KEY, " +
                                EVENT_CODE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                VALUE_NAME + " TEXT NOT NULL, " +
                                VALUE + " TEXT NOT NULL)";
            }

            static class Parameter{
                public static final String TABLE_NAME = "info_parameter";

                public static final String INFO_ID = Info.INFO_ID;
                public static final String PARAMETER_ID = "parameterId";
                public static final String VALUE_NAME = "valueName";
                public static final String VALUE = "value";

                public static final String CREATE_TABLE =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                                INFO_ID + " TEXT PRIMARY KEY, " +
                                PARAMETER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                VALUE_NAME + " TEXT NOT NULL, " +
                                VALUE + " TEXT NOT NULL)";
            }

            static class Resource{
                public static final String TABLE_NAME = "info_resource";

                public static final String INFO_ID = Info.INFO_ID;
                public static final String RESOURCE_ID = "resId";
                public static final String RESOURCE_DESC = "resourceDesc";
                public static final String MIME_TYPE = "mimeType";
                public static final String SIZE = "size";
                public static final String URI = "uri";
                public static final String DEREF_URI = "derefUri";
                public static final String DIGEST = "digest";

                public static final String CREATE_TABLE =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                                INFO_ID + " TEXT PRIMARY KEY, " +
                                RESOURCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                RESOURCE_DESC + " TEXT NOT NULL, " +
                                MIME_TYPE + " TEXT, " +
                                SIZE + " INTEGER, " +
                                URI + " TEXT, " +
                                DEREF_URI + " TEXT, " +
                                DIGEST + " TEXT)";
            }

            static class Area{
                public static final String TABLE_NAME = "info_area";

                public static final String INFO_ID = Info.INFO_ID;
                public static final String AREA_ID = "areaId";
                public static final String AREA_DESC = "areaDesc";
                public static final String ALTITUDE = "altitude";
                public static final String CEILING = "ceiling";

                public static final String CREATE_TABLE =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                                INFO_ID + " TEXT PRIMARY KEY, " +
                                AREA_ID + " TEXT PRIMARY KEY, " +
                                AREA_DESC + " TEXT NOT NULL, " +
                                ALTITUDE + " TEXT, " +
                                CEILING + " TEXT)";

                static class Polygon{
                    public static final String TABLE_NAME = "info_area_polygon";

                    public static final String AREA_ID = Area.AREA_ID;
                    public static final String POLYGON_ID = "polygonId";
                    public static final String POLYGON = "polygon";

                    public static final String CREATE_TABLE =
                            "CREATE TABLE " + TABLE_NAME + " (" +
                                    AREA_ID + " TEXT PRIMARY KEY, " +
                                    POLYGON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    POLYGON + " TEXT NOT NULL)";
                }

                static class Circle{
                    public static final String TABLE_NAME = "info_area_circle";

                    public static final String AREA_ID = Area.AREA_ID;
                    public static final String CIRCLE_ID = "circleId";
                    public static final String CIRCLE = "circle";

                    public static final String CREATE_TABLE =
                            "CREATE TABLE " + TABLE_NAME + " (" +
                                    AREA_ID + " TEXT PRIMARY KEY, " +
                                    CIRCLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    CIRCLE + " TEXT NOT NULL)";
                }

                static class Geocode{
                    public static final String TABLE_NAME = "info_area_geocode";

                    public static final String AREA_ID = Area.AREA_ID;
                    public static final String GEOCODE_ID = "geoId";
                    public static final String VALUE_NAME = "valueName";
                    public static final String VALUE = "value";

                    public static final String CREATE_TABLE =
                            "CREATE TABLE " + TABLE_NAME + " (" +
                                    INFO_ID + " TEXT PRIMARY KEY, " +
                                    GEOCODE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    VALUE_NAME + " TEXT NOT NULL, " +
                                    VALUE + " TEXT NOT NULL)";
                }
            }

        }
    }
    //endregion

}
