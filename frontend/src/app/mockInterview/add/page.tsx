"use client";
import { Button, Form, Input, InputNumber, Select, message } from "antd";
import React, { useState } from "react";
import { addMockInterviewUsingPost } from "@/api/mockInterviewController";
import { useRouter } from "next/navigation";
import "./index.css";

interface Props {}

/**
 * 创建 AI 模拟面试页面
 * @param props
 * @constructor
 */
const CreateMockInterviewPage: React.FC<Props> = (props) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const interviewTypeOptions = [
    { label: "技术深挖", value: "技术深挖" },
    { label: "项目拷打", value: "项目拷打" },
    { label: "系统设计", value: "系统设计" },
    { label: "HR", value: "HR" },
  ];

  const difficultyOptions = [
    { label: "初级", value: "初级" },
    { label: "中等", value: "中等" },
    { label: "高级", value: "高级" },
  ];

  /**
   * 提交表单
   *
   * @param values
   */
  const doSubmit = async (values: API.MockInterviewAddRequest) => {
    const hide = message.loading("正在创建模拟面试...");
    setLoading(true);
    try {
      const res = await addMockInterviewUsingPost(values);
      hide();
      message.success("模拟面试创建成功");
      form.resetFields(); // 重置表单
      // 跳转到模拟面试列表页面
      router.push("/mockInterview/chat/" + res.data);
    } catch (error: any) {
      hide();
      message.error("创建失败，" + error.message);
    }
    setLoading(false);
  };

  return (
    <div id="createMockInterviewPage">
      <div className="create-panel">
        <div className="create-header">
          <div className="eyebrow">AI Mock Interview</div>
          <h2>创建更像真实现场的模拟面试</h2>
          <p>
            补充岗位、面试类型、技术方向和项目背景后，系统会按轮次追问，并在结束时生成结构化复盘报告。
          </p>
        </div>

        <Form
          form={form}
          layout="vertical"
          style={{ marginTop: 24 }}
          onFinish={doSubmit}
          initialValues={{
            difficulty: "中等",
            interviewType: "技术深挖",
            expectedRounds: 5,
          }}
        >
        {/* 工作岗位 */}
        <Form.Item
          label="目标岗位"
          name="jobPosition"
          rules={[{ required: true, message: "请输入目标岗位" }]}
        >
          <Input placeholder="请输入工作岗位，例如：Java 开发工程师" />
        </Form.Item>

        {/* 工作年限 */}
        <Form.Item label="工作年限" name="workExperience">
          <Input placeholder="请输入工作年限，例如：3 年" />
        </Form.Item>

        <div className="create-grid">
          <Form.Item label="面试类型" name="interviewType">
            <Select options={interviewTypeOptions} placeholder="请选择面试类型" />
          </Form.Item>

          <Form.Item label="面试难度" name="difficulty">
            <Select options={difficultyOptions} placeholder="请选择面试难度" />
          </Form.Item>

          <Form.Item label="计划轮次" name="expectedRounds">
            <InputNumber min={3} max={8} style={{ width: "100%" }} />
          </Form.Item>
        </div>

        <Form.Item label="技术方向 / 技术栈" name="techStack">
          <Input placeholder="例如：Java、Spring Boot、MySQL、Redis、消息队列" />
        </Form.Item>

        <Form.Item label="简历 / 项目背景" name="resumeText">
          <Input.TextArea
            rows={6}
            placeholder="建议粘贴项目经历、职责、业务场景、性能指标或最近准备的重点方向，系统会更像真实面试官一样围绕这些信息追问。"
          />
        </Form.Item>

        {/* 面试难度 */}
        <Form.Item>
          <Button
            loading={loading}
            style={{ width: 220 }}
            type="primary"
            htmlType="submit"
          >
            创建并进入模拟面试
          </Button>
        </Form.Item>
        </Form>
      </div>
    </div>
  );
};

export default CreateMockInterviewPage;
