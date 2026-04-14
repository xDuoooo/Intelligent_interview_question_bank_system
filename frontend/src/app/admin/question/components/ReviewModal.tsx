import { reviewQuestionUsingPost } from "@/api/questionController";
import { QUESTION_REVIEW_STATUS_ENUM, QUESTION_REVIEW_STATUS_OPTIONS } from "@/constants/question";
import { Form, Input, message, Modal, Select } from "antd";
import React, { useEffect } from "react";

interface Props {
  open: boolean;
  question?: API.Question;
  onCancel: () => void;
  onSuccess: () => void;
}

const ReviewModal: React.FC<Props> = ({ open, question, onCancel, onSuccess }) => {
  const [form] = Form.useForm<API.QuestionReviewRequest>();
  const reviewStatus = Form.useWatch("reviewStatus", form);

  useEffect(() => {
    if (open) {
      form.setFieldsValue({
        id: question?.id,
        reviewStatus:
          question?.reviewStatus === QUESTION_REVIEW_STATUS_ENUM.REJECTED
            ? QUESTION_REVIEW_STATUS_ENUM.REJECTED
            : QUESTION_REVIEW_STATUS_ENUM.APPROVED,
        reviewMessage: question?.reviewMessage,
      });
    } else {
      form.resetFields();
    }
  }, [form, open, question]);

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const hide = message.loading("正在提交审核结果");
    try {
      await reviewQuestionUsingPost(values);
      hide();
      message.success("审核结果已保存");
      onSuccess();
    } catch (error: any) {
      hide();
      message.error("审核失败，" + (error?.message || "请稍后重试"));
    }
  };

  return (
    <Modal
      title={`审核题目${question?.title ? `：${question.title}` : ""}`}
      open={open}
      onCancel={onCancel}
      onOk={handleSubmit}
      okText="提交审核"
      cancelText="取消"
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item name="id" hidden>
          <Input />
        </Form.Item>
        <Form.Item
          label="审核结果"
          name="reviewStatus"
          rules={[{ required: true, message: "请选择审核结果" }]}
        >
          <Select
            options={QUESTION_REVIEW_STATUS_OPTIONS.filter(
              (item) =>
                item.value !== QUESTION_REVIEW_STATUS_ENUM.PENDING &&
                item.value !== QUESTION_REVIEW_STATUS_ENUM.PRIVATE,
            )}
          />
        </Form.Item>
        <Form.Item
          label="审核意见"
          name="reviewMessage"
          rules={[
            {
              required: reviewStatus === QUESTION_REVIEW_STATUS_ENUM.REJECTED,
              message: "驳回时请填写审核意见",
            },
            { max: 512, message: "审核意见不能超过 512 个字符" },
          ]}
          extra="通过时可以留空；驳回时建议明确指出需要修改的地方。"
        >
          <Input.TextArea rows={4} placeholder="请输入审核意见" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default ReviewModal;
