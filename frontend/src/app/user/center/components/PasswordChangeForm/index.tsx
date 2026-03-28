import { Button, Form, Input, message } from "antd";
import { changePasswordUsingPost } from "@/api/userController";
import React from "react";
import { useRouter } from "next/navigation";
import { useDispatch } from "react-redux";
import { AppDispatch } from "@/stores";
import { setLoginUser } from "@/stores/loginUser";
import { DEFAULT_USER } from "@/constants/user";

interface Props {
  onSuccess?: () => void;
}

/**
 * 修改密码表单
 * @constructor
 */
const PasswordChangeForm: React.FC<Props> = ({ onSuccess }) => {
  const [form] = Form.useForm();
  const router = useRouter();
  const dispatch = useDispatch<AppDispatch>();

  const doSubmit = async (values: API.UserChangePasswordRequest) => {
    const hide = message.loading("正在操作");
    try {
      await changePasswordUsingPost(values);
      hide();
      message.success("修改成功，请记住你的新密码");
      form.resetFields();
      dispatch(setLoginUser(DEFAULT_USER));
      onSuccess?.();
      router.replace(`/user/login?msg=${encodeURIComponent("密码修改成功，请重新登录")}`);
    } catch (error: any) {
      hide();
      message.error("修改失败，" + (error?.message || "请稍后重试"));
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
      <Form.Item
        label="旧密码"
        name="oldPassword"
        rules={[{ required: true, message: "请输入旧密码" }]}
      >
        <Input.Password placeholder="请输入旧密码" />
      </Form.Item>
      <Form.Item
        label="新密码"
        name="newPassword"
        rules={[
          { required: true, message: "请输入新密码" },
          { min: 8, message: "密码长度不能少于 8 位" },
        ]}
      >
        <Input.Password placeholder="请输入新密码" />
      </Form.Item>
      <Form.Item
        label="确认密码"
        name="checkPassword"
        rules={[
          { required: true, message: "请确认新密码" },
          ({ getFieldValue }) => ({
            validator(_, value) {
              if (!value || getFieldValue("newPassword") === value) {
                return Promise.resolve();
              }
              return Promise.reject(new Error("两次输入的密码不一致"));
            },
          }),
        ]}
      >
        <Input.Password placeholder="请确认新密码" />
      </Form.Item>
      <Form.Item>
        <Button style={{ width: 180 }} type="primary" htmlType="submit">
          修改密码
        </Button>
      </Form.Item>
    </Form>
  );
};

export default PasswordChangeForm;
