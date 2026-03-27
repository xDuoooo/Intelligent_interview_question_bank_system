import { Button, Form, Input, message } from "antd";
import { updateMyUserUsingPost } from "@/api/userController";
import React from "react";
import {useDispatch} from "react-redux";
import {AppDispatch} from "@/stores";
import loginUser, {setLoginUser} from "@/stores/loginUser";

interface Props {
  user: API.LoginUserVO;
}

/**
 * 用户信息编辑表单
 * @constructor
 */
const UserInfoEditForm = (props: Props) => {
  const dispatch = useDispatch<AppDispatch>();

  const [form] = Form.useForm();
  const { user } = props;
  form.setFieldsValue(user);

  /**
   * 提交
   *
   * @param values
   */
  const doSubmit = async (values: API.UserUpdateMyRequest) => {
    const hide = message.loading("正在操作");
    try {
      await updateMyUserUsingPost(values);
      hide();
      message.success("操作成功");
      // 更新全局登录状态
      dispatch(setLoginUser({ ...user, ...values }));
    } catch (error: any) {
      hide();
      message.error("操作失败，" + error.message);
    }
  };

  return (
    <Form
      form={form}
      style={{ marginTop: 24, maxWidth: 480 }}
      labelCol={{ span: 5 }}
      labelAlign="left"
      onFinish={doSubmit}
    >
      <Form.Item label="昵称" name="userName">
        <Input placeholder="请输入昵称" />
      </Form.Item>
      <Form.Item label="简介" name="userProfile">
        <Input.TextArea placeholder="请输入简介" />
      </Form.Item>
      <Form.Item label="手机号" name="phone">
        <Input placeholder="请输入手机号" />
      </Form.Item>
      <Form.Item label="邮箱" name="email">
        <Input placeholder="请输入邮箱" />
      </Form.Item>
      <Form.Item>
        <Button style={{ width: 180 }} type="primary" htmlType="submit">
          提交
        </Button>
      </Form.Item>
    </Form>
  );
};

export default UserInfoEditForm;