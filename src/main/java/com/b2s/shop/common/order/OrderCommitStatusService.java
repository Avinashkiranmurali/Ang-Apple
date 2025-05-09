package com.b2s.shop.common.order;

import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.dao.OrderCommitStatusDao;
import com.b2s.apple.entity.OrderCommitStatusEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderCommitStatusService {

	@Autowired
	private OrderCommitStatusDao orderCommitStatusDao;

	private static Logger logger = LoggerFactory.getLogger(OrderCommitStatusService.class);


	public boolean startPlacingOrder(String userId, String varId, String programId, Order order){
		boolean result = false;
		logger.info("Start placing order for User {}, Var {}, Program {}" , userId, varId, programId);
		if (hasPendingOrder(userId, varId, programId)){
			logger.debug("{} {} {} is placing an order while another ordering is being placed", userId, varId,
				programId);
			String content = this.getOrderDescription(order)+"<br>Host IP: "+HostInfoService.getService().getIP()+"<br>Time: "+new Date().toString();
			logger.info(content);
		}else{
			logger.debug("PASS! No pending order for User {}, Var {}, Program {}" , userId, varId, programId);
			markPendingOrder(userId, varId, programId, order);
			result = true;
		}		
		return result;
	}
	
	public boolean endPlacingOrder(String userId, String varId, String programId, Order order){
		boolean result = false;
		this.unmarkPendingOrder(userId, varId, programId ,order);	
		return result;
	}

	public boolean hasPendingOrder(String userId, String varId, String programId) {
		boolean result = false;
		final List<OrderCommitStatusEntity> list = orderCommitStatusDao.findByVarIdAndProgramIdAndUserId(varId, programId, userId);
		if (list != null && !list.isEmpty()) {
			result = true;
			logger.warn("{} {} {}  HAS PENDING ORDER! SEND EMAIL ALERT!", userId, varId, programId);
		}
		return result;
	}

	public void markPendingOrder(String userId, String varId, String programId, Order order) {
		String ip = HostInfoService.getService().getIP();
		OrderCommitStatusEntity record = new OrderCommitStatusEntity();
		record.setVarId(varId);
		record.setProgramId(programId);
		record.setUserId(userId);
		record.setOrderHashCode(this.getOrderHashCode(order));
		String desc = this.getOrderDescription(order);
		record.setOrderDescription(desc);
		record.setInsertTime(new Date());
		if (ip.length() > 500) {
			ip = ip.substring(0, 500);
		}
		record.setAttr1(ip);
		orderCommitStatusDao.save(record);
		logger.info("{} {} {}: mark a pending order.{}", userId, varId, programId, desc);
	}

	private void unmarkPendingOrder(String userId, String varId, String programId, Order order) {

		int hashCode = this.getOrderHashCode(order);
		long cnt = orderCommitStatusDao.deleteByVarIdAndProgramIdAndUserIdAndOrderHashCode(varId, programId, userId, hashCode);
		if (cnt == 0) {
			logger.info("Delete with out hash");
			cnt = orderCommitStatusDao.deleteByVarIdAndProgramIdAndUserIdAndOrderHashCode(varId, programId, userId, 0);
		}

		logger.info("{} {} {}: Unmark a pending order (hash: {}). deleted {} records", userId, varId, programId,
			hashCode, cnt);
	}
	
	private int getOrderHashCode(Order order){
		return Optional.ofNullable(order.getOrderLines())
			.map(list -> order.getOrderLines()
				.stream()
				.map(line -> ((OrderLine) line).getName())
				.collect(Collectors.joining())
				.hashCode())
			.orElse(0);
	}
	
	public String getOrderDescription(Order order){
		List<OrderLine> lines = order.getOrderLines();
		final String description = Optional.ofNullable(lines)
			.map(list -> lines
				.stream()
				.map(line -> "{" + line.getItemId() + ":" + line.getName() + ":" + line.getQuantity() + "}")
				.collect(Collectors.joining())
			)
			.orElse("No Amazon Items in this order");
		// we must truncate in order to fit inside the order_commit_status.order_description column
		return String.format("%.8000s", description);
	}

}
