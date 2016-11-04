package im.vinci.server.naturelang.service.back;

/**
 * Created by mlc on 2016/7/30.
 */

public class WeatherBack {
    private int rc;
    private String text;
    private String rtext;
    private String service;
    private String operation;
    private Semantic semantic;
    class Semantic{
        private Location location;
        private Datetime datetime;
        private Slots slots;
        class Location{
            private String cityAddr;
            private String city;
            private String type;
            public String getCityAddr() {
                return cityAddr;
            }
            public void setCityAddr(String cityAddr) {
                this.cityAddr = cityAddr;
            }
            public String getCity() {
                return city;
            }
            public void setCity(String city) {
                this.city = city;
            }
            public String getType() {
                return type;
            }
            public void setType(String type) {
                this.type = type;
            }
        }
        class Datetime{
            private String date;
            private String type;
            private String dateOrig;
            public String getDate() {
                return date;
            }
            public void setDate(String date) {
                this.date = date;
            }
            public String getType() {
                return type;
            }
            public void setType(String type) {
                this.type = type;
            }
            public String getDateOrig() {
                return dateOrig;
            }
            public void setDateOrig(String dateOrig) {
                this.dateOrig = dateOrig;
            }
        }
        class Slots{
            private String airQuality;
            private String sourceName;
            private String date;
            private String lastUpdateTime;
            private String dateLong;
            private String city;
            private String wind;
            private String windLevel;
            private String weather;
            private String tempRange;
            private String province;
            public String getAirQuality() {
                return airQuality;
            }
            public void setAirQuality(String airQuality) {
                this.airQuality = airQuality;
            }
            public String getSourceName() {
                return sourceName;
            }
            public void setSourceName(String sourceName) {
                this.sourceName = sourceName;
            }
            public String getDate() {
                return date;
            }
            public void setDate(String date) {
                this.date = date;
            }
            public String getLastUpdateTime() {
                return lastUpdateTime;
            }
            public void setLastUpdateTime(String lastUpdateTime) {
                this.lastUpdateTime = lastUpdateTime;
            }
            public String getDateLong() {
                return dateLong;
            }
            public void setDateLong(String dateLong) {
                this.dateLong = dateLong;
            }
            public String getCity() {
                return city;
            }
            public void setCity(String city) {
                this.city = city;
            }
            public String getWind() {
                return wind;
            }
            public void setWind(String wind) {
                this.wind = wind;
            }
            public String getWindLevel() {
                return windLevel;
            }
            public void setWindLevel(String windLevel) {
                this.windLevel = windLevel;
            }
            public String getWeather() {
                return weather;
            }
            public void setWeather(String weather) {
                this.weather = weather;
            }
            public String getTempRange() {
                return tempRange;
            }
            public void setTempRange(String tempRange) {
                this.tempRange = tempRange;
            }
            public String getProvince() {
                return province;
            }
            public void setProvince(String province) {
                this.province = province;
            }
        }
        public Location getLocation() {
            return location;
        }
        public void setLocation(Location location) {
            this.location = location;
        }
        public Datetime getDatetime() {
            return datetime;
        }
        public void setDatetime(Datetime datetime) {
            this.datetime = datetime;
        }
        public Slots getSlots() {
            return slots;
        }
        public void setSlots(Slots slots) {
            this.slots = slots;
        }
    }
    public int getRc() {
        return rc;
    }
    public void setRc(int rc) {
        this.rc = rc;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getRtext() {
        return rtext;
    }
    public void setRtext(String rtext) {
        this.rtext = rtext;
    }
    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
    public Semantic getSemantic() {
        return semantic;
    }
    public void setSemantic(Semantic semantic) {
        this.semantic = semantic;
    }

}


