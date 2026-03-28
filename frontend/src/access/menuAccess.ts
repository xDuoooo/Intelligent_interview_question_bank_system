import type { MenuDataItem } from "@ant-design/pro-layout";
import { menus } from "../../config/menu";
import checkAccess from "@/access/checkAccess";

/**
 * 获取有权限、可访问的菜单（递归）
 * @param loginUser
 * @param menuItems
 */
const getAccessibleMenus = (
  loginUser: API.LoginUserVO,
  menuItems = menus,
): MenuDataItem[] => {
  return menuItems
    .filter((item) => {
      if (item.hideInMenu) {
        return false;
      }
      return checkAccess(loginUser, item.access);
    })
    .map((item) => ({
      ...item,
      children: item.children ? getAccessibleMenus(loginUser, item.children) : undefined,
    }));
};

export default getAccessibleMenus;
