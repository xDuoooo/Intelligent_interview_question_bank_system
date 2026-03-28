"use client";

import React, { useCallback, useEffect, useState } from "react";
import { Provider, useDispatch } from "react-redux";
import store, { AppDispatch } from "@/stores";
import { getLoginUserUsingGet } from "@/api/userController";
import { setLoginUser } from "@/stores/loginUser";
import { AntdRegistry } from "@ant-design/nextjs-registry";
import { AuthInitContext } from "@/contexts/AuthInitContext";

/**
 * 全局初始化组件
 */
const InitLayout: React.FC<{
  children: React.ReactNode;
}> = ({ children }) => {
  const dispatch = useDispatch<AppDispatch>();
  const [authInitialized, setAuthInitialized] = useState(false);
  
  const doInitLoginUser = useCallback(async () => {
    try {
      const res = await getLoginUserUsingGet();
      if (res.data) {
        dispatch(setLoginUser(res.data as API.LoginUserVO));
      }
    } finally {
      setAuthInitialized(true);
    }
  }, [dispatch]);

  useEffect(() => {
    void doInitLoginUser();
  }, [doInitLoginUser]);

  return <AuthInitContext.Provider value={authInitialized}>{children}</AuthInitContext.Provider>;
};

/**
 * 客户端布局包装器
 */
export default function ClientLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AntdRegistry>
      <Provider store={store}>
        <InitLayout>{children}</InitLayout>
      </Provider>
    </AntdRegistry>
  );
}
