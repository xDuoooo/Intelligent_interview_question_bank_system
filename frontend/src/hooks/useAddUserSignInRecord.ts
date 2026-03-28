import { useCallback, useEffect, useState } from "react";
import { addUserSignInUsingPost } from "@/api/userController";
import { useSelector } from "react-redux";
import { RootState } from "@/stores";
import ACCESS_ENUM from "@/access/accessEnum";

/**
 * 添加用户刷题签到记录钩子
 * @constructor
 */
const useAddUserSignInRecord = () => {
  // 签到状态
  const [loading, setLoading] = useState<boolean>(true);
  const loginUser = useSelector((state: RootState) => state.loginUser);

  // 请求后端执行签到
  const doFetch = useCallback(async () => {
    // 如果未登录，不执行签到逻辑
    if (!loginUser || !loginUser.id || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      await addUserSignInUsingPost({});
    } catch (e: any) {
      // 静默处理错误，或仅记录日志，避免干扰未登录/权限异常的用户
      console.error("获取刷题签到记录失败，" + e.message);
    }
    setLoading(false);
  }, [loginUser]);

  // 依赖 loginUser.id，确保用户登录状态变化时能正确触发
  useEffect(() => {
    void doFetch();
  }, [doFetch]);

  return { loading };
};

export default useAddUserSignInRecord;
