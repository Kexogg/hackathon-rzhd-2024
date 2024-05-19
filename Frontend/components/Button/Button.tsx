import React, {ButtonHTMLAttributes, DetailedHTMLProps, ReactNode} from 'react';

type ButtonProps = DetailedHTMLProps<ButtonHTMLAttributes<HTMLButtonElement>, HTMLButtonElement> & {
    children: ReactNode;
}

const Button = ({children, ...props}: ButtonProps) => {
    return (
        <button {...props} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
            {children}
        </button>
    );
};

export default Button;
