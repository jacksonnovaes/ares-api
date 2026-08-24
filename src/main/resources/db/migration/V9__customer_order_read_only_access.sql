-- Contas exclusivas de cliente final podem consultar somente as próprias ordens.
DELETE FROM user_permissions permission
WHERE permission.permission <> 'SERVICE_ORDER_READ'
  AND EXISTS (
      SELECT 1 FROM user_roles customer_role
      WHERE customer_role.user_id = permission.user_id
        AND customer_role.role = 'CUSTOMER'
  )
  AND NOT EXISTS (
      SELECT 1 FROM user_roles staff_role
      WHERE staff_role.user_id = permission.user_id
        AND staff_role.role <> 'CUSTOMER'
  );

INSERT INTO user_permissions (user_id, permission)
SELECT customer_role.user_id, 'SERVICE_ORDER_READ'
FROM user_roles customer_role
WHERE customer_role.role = 'CUSTOMER'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles staff_role
      WHERE staff_role.user_id = customer_role.user_id
        AND staff_role.role <> 'CUSTOMER'
  )
ON CONFLICT (user_id, permission) DO NOTHING;
