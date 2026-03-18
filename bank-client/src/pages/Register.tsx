import { useState } from "react";
import { useNavigate } from "react-router";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../components/ui/card";
import { BrandLogo } from "../components/BrandLogo";
import { toast } from "sonner";
import { useRegisterMutation } from "../lib/api/generatedApi";
import { setTokens } from "../lib/auth/tokenStorage";

export function Register() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [register, { isLoading }] = useRegisterMutation();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      toast.error("Пароли не совпадают");
      return;
    }

    if (password.length < 6) {
      toast.error("Пароль должен содержать минимум 6 символов");
      return;
    }

    try {
      const jwt = await register({
        registerModelDto: {
          name,
          email,
          password,
        },
      }).unwrap();

      setTokens(jwt);
      toast.success("Регистрация прошла успешно");
      navigate("/dashboard");
    } catch (error) {
      toast.error("Не удалось завершить регистрацию");
    }
  };

  return (
    <div className="dark min-h-screen flex items-center justify-center p-4 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-primary/20 via-background to-background">
      <Card className="w-full max-w-md border-2 shadow-2xl">
        <CardHeader className="space-y-4 text-center">
          <div className="flex justify-center">
            <BrandLogo size="xl" />
          </div>
          <div>
            <CardTitle className="text-3xl mb-1">Регистрация</CardTitle>
            <CardDescription className="text-base">
              Создайте новый аккаунт для доступа к банковским услугам
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleRegister} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name" className="text-base">Имя</Label>
              <Input
                id="name"
                type="text"
                placeholder="Иван Иванов"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                className="bg-input h-12 text-base"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email" className="text-base">Электронная почта</Label>
              <Input
                id="email"
                type="email"
                placeholder="your@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="bg-input h-12 text-base"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password" className="text-base">Пароль</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="bg-input h-12 text-base"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword" className="text-base">Подтвердите пароль</Label>
              <Input
                id="confirmPassword"
                type="password"
                placeholder="••••••••"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                className="bg-input h-12 text-base"
              />
            </div>
            <Button type="submit" className="w-full h-12 text-lg" disabled={isLoading}>
              Зарегистрироваться
            </Button>
            <div className="text-center pt-2">
              <button
                type="button"
                onClick={() => navigate("/")}
                className="text-sm text-muted-foreground hover:text-primary transition-colors underline-offset-4 hover:underline"
              >
                Уже есть аккаунт? Войти
              </button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}