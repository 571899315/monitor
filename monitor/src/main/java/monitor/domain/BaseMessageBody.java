package monitor.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

public class BaseMessageBody<T> {
	/**
	 * UUID,涓嶄负绌猴紝鏋勯�犳柟娉曡嚜鍔ㄧ敓鎴�
	 */
	@JSONField(name = "eventId")
	private String eventId;
	/**
	 * 鏃堕棿鎴�,涓嶄负绌猴紝鏍煎紡涓� yyyy-MM-dd HH:mm:ss.SSS
	 */
	@JSONField(name = "eventTs")
	private String eventTs;
	/**
	 * 瀹㈡埛绔被鍨嬶紙1锛欼OS锛�2锛氬畨鍗擄級锛屼笉涓虹┖锛宺equest涓幏鍙�
	 */
	@JSONField(name = "platformType")
	private Integer platformType;
	/**
	 * 鎵╁睍淇℃伅锛屽彲閫�
	 */
	@JSONField(name = "eventContent")
	private T eventContent;
	/**
	 * 鐢ㄦ埛id锛屼笉涓虹┖锛宺equest涓幏鍙�
	 */
	@JSONField(name = "userId")
	private Integer userId;
	/**
	 * 鐘舵�� 瑙丩oanMonitorStatusEnum锛屽彲閫�
	 */
	@JSONField(name = "status")
	private String status;
	/**
	 * 涓氬姟id锛屽彲閫�
	 */
	@JSONField(name = "businessNo")
	private String businessNo;

	public BaseMessageBody() {
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventTs() {
		return eventTs;
	}

	public void setEventTs(String eventTs) {
		this.eventTs = eventTs;
	}

	public Integer getPlatformType() {
		return platformType;
	}

	public void setPlatformType(Integer platformType) {
		this.platformType = platformType;
	}

	public T getEventContent() {
		return eventContent;
	}

	public void setEventContent(T eventContent) {
		this.eventContent = eventContent;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBusinessNo() {
		return businessNo;
	}

	public void setBusinessNo(String businessNo) {
		this.businessNo = businessNo;
	}

	public BaseMessageBody(Date aEventTs, Integer aPlatformType, T aEventContent, Integer aUserId, String aStatus,
			String aBusinessNo) {
		this.eventId = UUID.randomUUID().toString();
		this.eventTs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(aEventTs);
		this.platformType = aPlatformType;
		this.eventContent = aEventContent;
		this.userId = aUserId;
		this.status = aStatus;
		if (aBusinessNo != null) {
			this.businessNo = aBusinessNo;
		} else if (aUserId != null) {
			this.businessNo = aUserId.toString();
		}
	}

}
