package com.b2s.apple.services;

import com.b2s.apple.entity.MessageExceptionsEntity;
import com.b2s.apple.entity.MessagesEntity;
import com.b2s.rewards.apple.dao.MessageExceptionsDao;
import com.b2s.rewards.apple.dao.MessagesDao;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.util.USER_MSG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

	@Autowired
	private MessagesDao messagesDao;

	@Autowired
	private MessageExceptionsDao messageExceptionsDao;

	public MessagesEntity selectMessage(String varid, String programid, int supplierid, int code) {
		if (supplierid > 30 && supplierid < 38) {
			supplierid = CommonConstants.SUPPLER_TYPE_TRAVEL;
		}
		if (code == USER_MSG.CS_VOUCHER_MSG) {
			supplierid = -1;
			code = USER_MSG.CUSTOMER_SERVICE_CONTACT;
		}

		return selectPlatformMessage(varid, programid, supplierid, code);
	}

	private MessagesEntity selectPlatformMessage(String varid, String programid, int supplierid, int code) {
		try {
			final MessagesEntity.MessageId messageId = new MessagesEntity.MessageId();

			messageId.setCode(code);
			messageId.setProgramId(programid);
			messageId.setSupplierId(supplierid);
			messageId.setVarId(varid);

			MessagesEntity msg = messagesDao.get(messageId);
			if ((msg != null) && (msg.getMessage() != null) && (!msg.getMessage().isEmpty())) {
				return msg;
			} else {
				messageId.setCode(code);
				messageId.setProgramId("-1");
				messageId.setSupplierId(supplierid);
				messageId.setVarId(varid);

				msg = messagesDao.get(messageId);
				if ((msg != null) && (msg.getMessage() != null) && (!msg.getMessage().isEmpty())) {

					return msg;
				} else {
					messageId.setCode(code);
					messageId.setProgramId("-1");
					messageId.setSupplierId(supplierid);
					messageId.setVarId("-1");

					msg = messagesDao.get(messageId);
					if ((msg != null) && (msg.getMessage() != null)
						&& (!msg.getMessage().isEmpty())) {
						return msg;
					} else {
						return null;
					}
				}
			}
		} catch (final Exception ex) {
			logger.error("Unknown Error ", ex);
			return null;
		}
	}

	public void insertMessageException(User user, String classname,
			String descn, Exception ex) {
		MessageExceptionsEntity exp = new MessageExceptionsEntity();
		String txt = "";
		try {
			if (ex != null) {
				txt = ex.getMessage();
				StackTraceElement elem = null;
				for (int y = 0; y < ex.getStackTrace().length; y++) {
					elem = ex.getStackTrace()[y];
					txt += elem.getClassName() + "." + elem.getMethodName()
							+ "(" + elem.getLineNumber() + ")     " + CommonConstants.cr;
				}
			}
			if (user != null) {
				exp.setVarId(user.getVarId());
				exp.setProgramid(user.getProgramId());
			} else {
				exp.setVarId("-1");
				exp.setProgramid("-1");
			}
			exp.setClassname(classname);
			exp.setDescn(descn);
			exp.setDetail(txt);
			exp.setCreateDatetime(new Timestamp(System.currentTimeMillis()));

			messageExceptionsDao.saveOrUpdate(exp);
		} catch (Exception ec) {
			logger.error("",ec);
		}

	}

	public static String replaceOrderIdToken(String orderid, String message) {
		if (message == null)
			return "";
		return message.replaceAll(CommonConstants.TOKEN_ORDERID, orderid);
	}

	public String getMessage(final String varId, final String programId, final int supplierId, final int code) {

		final MessagesEntity message = selectMessage(varId, programId, supplierId, code);
		return (message != null) ? message.getMessage() : null;
	}

}
