import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../components/ui/card";
import { BrandLogo } from "../components/BrandLogo";
import { toast } from "sonner";
import { useGetUserQuery, useLoginMutation } from "../lib/api/generatedApi";
import { getRefreshToken, setTokens } from "../lib/auth/tokenStorage";

export function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [login, { isLoading }] = useLoginMutation();
  const {data: user} = useGetUserQuery();

  useEffect(() => {
    if (user){
      navigate('/dashboard')
    }
  }, [user])

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const jwt = await login({
        loginModelDto: {
          email,
          password,
        },
      }).unwrap();

      setTokens(jwt);
      toast.success("Вход выполнен успешно");
      queueMicrotask(() => navigate("/dashboard"));
      //navigate("/dashboard");
    } catch (error) {
      toast.error("Неверная почта или пароль");
    }
  };

  return (
    <div className="dark min-h-screen flex items-center justify-center p-4 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-primary/20 via-background to-background">
      <Card className="w-full max-w-md border-2 shadow-2xl">
        <CardHeader className="space-y-6 text-center">
          <div className="flex justify-center">
            <BrandLogo size="xl" />
          </div>
          <div>
            <CardTitle className="text-3xl mb-2">Вход в систему</CardTitle>
            <CardDescription className="text-base">
              Введите свои данные для входа в личный кабинет
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleLogin} className="space-y-6">
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
            <Button type="submit" className="w-full h-12 text-lg" disabled={isLoading}>
              Войти
            </Button>
            <div className="text-center pt-2">
              <button
                type="button"
                onClick={() => navigate("/register")}
                className="text-sm text-muted-foreground hover:text-primary transition-colors underline-offset-4 hover:underline"
              >
                Нет аккаунта? Зарегистрироваться
              </button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}