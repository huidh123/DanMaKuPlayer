package com.example.javaBean;


public class AVBaseInfo {
	private String typename;
	private Integer play;
	private Integer review;
	private Integer video_review;
	private Integer favorites;
	private String title ;
	private String tag;
	private String pic;
	private String author;
	private Integer coins;
	private String created_at;
	private Integer cid;
	public String getTypename() {
		return typename;
	}
	public Integer getPlay() {
		return play;
	}
	public void setPlay(Integer play) {
		this.play = play;
	}
	public Integer getReview() {
		return review;
	}
	public void setReview(Integer review) {
		this.review = review;
	}
	public Integer getVideo_review() {
		return video_review;
	}
	public void setVideo_review(Integer video_review) {
		this.video_review = video_review;
	}
	public Integer getFavorites() {
		return favorites;
	}
	public void setFavorites(Integer favorites) {
		this.favorites = favorites;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public Integer getCoins() {
		return coins;
	}
	public void setCoins(Integer coins) {
		this.coins = coins;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public Integer getCid() {
		return cid;
	}
	public void setCid(Integer cid) {
		this.cid = cid;
	}
	public void setTypename(String typename) {
		this.typename = typename;
	}
	public AVBaseInfo(String typename, Integer play, Integer review,
			Integer video_review, Integer favorites, String title, String tag,
			String pic, String author, Integer coins, String created_at,
			Integer cid) {
		super();
		this.typename = typename;
		this.play = play;
		this.review = review;
		this.video_review = video_review;
		this.favorites = favorites;
		this.title = title;
		this.tag = tag;
		this.pic = pic;
		this.author = author;
		this.coins = coins;
		this.created_at = created_at;
		this.cid = cid;
	}
	
	
}
