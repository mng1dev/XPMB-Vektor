package com.raddstudios.xpmb.utils;

public class Serialization {
	public static class gameClass {
		private String gameTitle = null;
		private String coverURL = null;

		public gameClass(String title, String url) {
			gameTitle = title;
			coverURL = url;
		}
		public void setTitle(String title){
			this.gameTitle=title;
		}
		public String getTitle(){
			return gameTitle;
		}
		public void setURL(String url){
			this.coverURL = url;
		}
		public String getURL(){
			return coverURL;
		}
		public void clear(){
			coverURL=null;
			gameTitle=null;
		}
	}
}
