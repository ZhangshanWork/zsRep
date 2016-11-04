package im.vinci.server.naturelang.domain;

public class Response {
	private int rc;
	private String text;
	private String rtext;
	private String service;
	private String operation;//create,view,delete
	private Semantic semantic = new Semantic();
	public class Semantic{
		private Slots slots = new Slots();
		private String content;
		public class Slots{
			private String name;
			private Datetime datetime = new Datetime();
			private String[] whiteNoise;
			public class Datetime{
				private String type;
				private String date;
				private String dateOrig="";
				private String time;
				private String timeOrig="";
				private boolean repeat;
				private long duration;
				private int index;
				private boolean count;
				public String getType() {
					return type;
				}
				public void setType(String type) {
					this.type = type;
				}
				public String getDate() {
					return date;
				}
				public void setDate(String date) {
					this.date = date;
				}
				public String getDateOrig() {
					return dateOrig;
				}
				public void setDateOrig(String dateOrig) {
					this.dateOrig = dateOrig;
				}
				public String getTime() {
					return time;
				}
				public void setTime(String time) {
					this.time = time;
				}
				public String getTimeOrig() {
					return timeOrig;
				}
				public void setTimeOrig(String timeOrig) {
					this.timeOrig = timeOrig;
				}
				public boolean isRepeat() {
					return repeat;
				}
				public void setRepeat(boolean repeat) {
					this.repeat = repeat;
				}
				public long getDuration() {
					return duration;
				}
				public void setDuration(long duration) {
					this.duration = duration;
				}
				public int getIndex() {
					return index;
				}
				public void setIndex(int index) {
					this.index = index;
				}
				public boolean getCount() {
					return count;
				}
				public void setCount(boolean count) {
					this.count = count;
				}
			}
			public Datetime getDatetime() {
				return datetime;
			}
			public void setDatetime(Datetime datetime) {
				this.datetime = datetime;
			}
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public void setWhiteNoise(String[] whiteNoise){this.whiteNoise = whiteNoise;}
			public String[] getWhiteNoise(){return whiteNoise;}
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public Slots getSlots() {
			return slots;
		}
		public void setSlots(Slots slots) {
			this.slots = slots;
		}
	}
	public Semantic getSemantic() {
		return semantic;
	}
	public void setSemantic(Semantic semantic) {
		this.semantic = semantic;
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

}
