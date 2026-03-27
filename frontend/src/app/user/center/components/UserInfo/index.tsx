import React from "react";
import Paragraph from "antd/es/typography/Paragraph";
import "./index.css";

interface Props {
  user: API.LoginUserVO;
}

/**
 * 用户个人资料
 * @param props
 * @constructor
 */
const UserInfo = (props: Props) => {
  const { user } = props;

  return (
    <div className="user-info">
      <div style={{ textAlign: "left" }}>
        {/* 手机号 */}
        <Paragraph type="secondary">
          手机号：{user.phone || "未填写"}
        </Paragraph>
        {/* 邮箱 */}
        <Paragraph type="secondary">邮箱：{user.email || "未填写"}</Paragraph>
      </div>
    </div>
  );
};

export default UserInfo;