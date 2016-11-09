package im.vinci.server.naturelang.domain;

public class PMResponse {
	private int rc;
	private String text;
	private String rtext;
	private String service;
	private String operation;
	private Semantic semantic = new Semantic();
	public class Semantic{
		private LocationResponse location = new LocationResponse();
		private Slots slots = new Slots();
		public class Slots{
			private String sourceName;
			private int aqi;
			private String publishDateTime;
			private String subArea;
			private long publishDateTimeLong;
			private int pm25;
			private String positionName;
			private String quality;
			private String area;
			public String getSourceName() {
				return sourceName;
			}
			public void setSourceName(String sourceName) {
				this.sourceName = sourceName;
			}
			public int getAqi() {
				return aqi;
			}
			public void setAqi(int aqi) {
				this.aqi = aqi;
			}
			public String getPublishDateTime() {
				return publishDateTime;
			}
			public void setPublishDateTime(String publishDateTime) {
				this.publishDateTime = publishDateTime;
			}
			public String getSubArea() {
				return subArea;
			}
			public void setSubArea(String subArea) {
				this.subArea = subArea;
			}
			public long getPublishDateTimeLong() {
				return publishDateTimeLong;
			}
			public void setPublishDateTimeLong(long publishDateTimeLong) {
				this.publishDateTimeLong = publishDateTimeLong;
			}
			public int getPm25() {
				return pm25;
			}
			public void setPm25(int pm25) {
				this.pm25 = pm25;
			}
			public String getPositionName() {
				return positionName;
			}
			public void setPositionName(String positionName) {
				this.positionName = positionName;
			}
			public String getQuality() {
				return quality;
			}
			public void setQuality(String quality) {
				this.quality = quality;
			}
			public String getArea() {
				return area;
			}
			public void setArea(String area) {
				this.area = area;
			}
		}
		public LocationResponse getLocation() {
			return location;
		}
		public void setLocation(LocationResponse location) {
			this.location = location;
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
